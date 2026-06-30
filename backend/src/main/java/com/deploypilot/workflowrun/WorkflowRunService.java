package com.deploypilot.workflowrun;

import com.deploypilot.ai.AiServiceClient;
import com.deploypilot.ai.ClassifyRequest;
import com.deploypilot.ai.ClassifyResponse;
import com.deploypilot.ai.ExtractRequest;
import com.deploypilot.ai.ExtractResponse;
import com.deploypilot.ai.GenerateActionRequest;
import com.deploypilot.ai.GenerateActionResponse;
import com.deploypilot.common.exception.ResourceNotFoundException;
import com.deploypilot.deployment.DeploymentConfig;
import com.deploypilot.deployment.DeploymentConfigRepository;
import com.deploypilot.deployment.DeploymentConfigStatus;
import com.deploypilot.deployment.DeploymentEnvironment;
import com.deploypilot.workflow.Workflow;
import com.deploypilot.workflow.WorkflowService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Service for managing {@link WorkflowRun} entities.
 * Orchestrates AI processing for each workflow run.
 */
@Service
@Transactional(readOnly = true)
public class WorkflowRunService {

    private static final Logger log = LoggerFactory.getLogger(WorkflowRunService.class);

    private final WorkflowRunRepository workflowRunRepository;
    private final WorkflowService workflowService;
    private final DeploymentConfigRepository deploymentConfigRepository;
    private final AiServiceClient aiServiceClient;
    private final ObjectMapper objectMapper;

    public WorkflowRunService(
            WorkflowRunRepository workflowRunRepository,
            WorkflowService workflowService,
            DeploymentConfigRepository deploymentConfigRepository,
            AiServiceClient aiServiceClient,
            ObjectMapper objectMapper
    ) {
        this.workflowRunRepository = workflowRunRepository;
        this.workflowService = workflowService;
        this.deploymentConfigRepository = deploymentConfigRepository;
        this.aiServiceClient = aiServiceClient;
        this.objectMapper = objectMapper;
    }

    public List<WorkflowRunResponse> findByWorkflowId(UUID workflowId) {
        workflowService.getEntityById(workflowId);
        return workflowRunRepository.findByWorkflowId(workflowId).stream()
                .map(WorkflowRunService::toResponse)
                .toList();
    }

    public WorkflowRunResponse findById(UUID id) {
        return workflowRunRepository.findById(id)
                .map(WorkflowRunService::toResponse)
                .orElseThrow(() -> new ResourceNotFoundException("Workflow run not found: " + id));
    }

    @Transactional
    public WorkflowRunResponse create(UUID workflowId, CreateWorkflowRunRequest request) {
        Workflow workflow = workflowService.getEntityById(workflowId);

        WorkflowRun workflowRun = new WorkflowRun();
        workflowRun.setWorkflow(workflow);
        workflowRun.setInputSource(request.inputSource());
        workflowRun.setInputContent(request.inputContent());
        workflowRun.setStatus(RunStatus.PROCESSING);

        workflowRun = workflowRunRepository.save(workflowRun);

        DeploymentConfig config = deploymentConfigRepository.findByCustomerIdAndEnvironmentAndStatus(
                        workflow.getCustomer().getId(), DeploymentEnvironment.PROD, DeploymentConfigStatus.ACTIVE)
                .orElse(null);

        try {
            double confidence = processAiWorkflow(workflowRun, workflow.getId(), config);
            
            boolean needsApproval = false;
            if (config != null) {
                if (config.isApprovalRequired()) {
                    needsApproval = true;
                } else if (confidence < config.getConfidenceThreshold()) {
                    needsApproval = true;
                }
            }

            if (needsApproval) {
                workflowRun.setStatus(RunStatus.WAITING_FOR_APPROVAL);
            } else {
                workflowRun.setStatus(RunStatus.COMPLETED);
            }
        } catch (Exception e) {
            log.error("AI processing failed for workflow run: {}", workflowRun.getId(), e);
            workflowRun.setStatus(RunStatus.FAILED);
        }

        return toResponse(workflowRunRepository.save(workflowRun));
    }

    private double processAiWorkflow(WorkflowRun workflowRun, UUID workflowId, DeploymentConfig config) {
        if (config != null && !config.isLlmEnabled()) {
            return processWithMockAi(workflowRun);
        }

        // 1. Classify
        ClassifyResponse classifyResponse = aiServiceClient.classify(new ClassifyRequest(
                workflowRun.getInputContent(),
                workflowRun.getInputSource(),
                workflowId.toString()
        ));
        workflowRun.setDetectedIntent(classifyResponse.detectedIntent());

        // 2. Extract
        ExtractResponse extractResponse = aiServiceClient.extract(new ExtractRequest(
                workflowRun.getInputContent(),
                workflowRun.getDetectedIntent()
        ));
        workflowRun.setExtractedFieldsJson(serializeFields(extractResponse.fields()));

        // 3. Generate Action
        GenerateActionResponse generateResponse = aiServiceClient.generate(new GenerateActionRequest(
                "Recommend the next action for this " + workflowRun.getDetectedIntent(),
                extractResponse.fields()
        ));
        workflowRun.setRecommendedAction(generateResponse.content());

        return classifyResponse.confidence();
    }

    private double processWithMockAi(WorkflowRun workflowRun) {
        log.info("LLM disabled for customer, using mock AI processing");
        workflowRun.setDetectedIntent("MOCK_INTENT");
        workflowRun.setExtractedFieldsJson(serializeFields(Collections.emptyMap()));
        workflowRun.setRecommendedAction("Mock recommendation because LLM is disabled.");
        return 1.0; // High confidence for mock
    }

    private String serializeFields(java.util.Map<String, Object> fields) {
        try {
            return objectMapper.writeValueAsString(fields);
        } catch (JsonProcessingException e) {
            log.warn("Failed to serialize extracted fields, saving as empty JSON", e);
            return "{}";
        }
    }

    static WorkflowRunResponse toResponse(WorkflowRun workflowRun) {
        return new WorkflowRunResponse(
                workflowRun.getId(),
                workflowRun.getWorkflow().getId(),
                workflowRun.getInputSource(),
                workflowRun.getInputContent(),
                workflowRun.getDetectedIntent(),
                workflowRun.getExtractedFieldsJson(),
                workflowRun.getRecommendedAction(),
                workflowRun.getStatus(),
                workflowRun.getCreatedAt(),
                workflowRun.getUpdatedAt()
        );
    }
}
