package com.deploypilot.workflowrun;

import com.deploypilot.ai.AiServiceClient;
import com.deploypilot.ai.ClassifyRequest;
import com.deploypilot.ai.ClassifyResponse;
import com.deploypilot.ai.ExtractRequest;
import com.deploypilot.ai.ExtractResponse;
import com.deploypilot.ai.GenerateActionRequest;
import com.deploypilot.ai.GenerateActionResponse;
import com.deploypilot.common.exception.ResourceNotFoundException;
import com.deploypilot.workflow.Workflow;
import com.deploypilot.workflow.WorkflowService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.List;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class WorkflowRunService {

    private static final Logger log = LoggerFactory.getLogger(WorkflowRunService.class);

    private final WorkflowRunRepository workflowRunRepository;
    private final WorkflowService workflowService;
    private final AiServiceClient aiServiceClient;
    private final ObjectMapper objectMapper;

    public WorkflowRunService(
            WorkflowRunRepository workflowRunRepository,
            WorkflowService workflowService,
            AiServiceClient aiServiceClient,
            ObjectMapper objectMapper
    ) {
        this.workflowRunRepository = workflowRunRepository;
        this.workflowService = workflowService;
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

        try {
            // 1. Classify
            ClassifyResponse classifyResponse = aiServiceClient.classify(new ClassifyRequest(
                    workflowRun.getInputContent(),
                    workflowRun.getInputSource(),
                    workflow.getId().toString()
            ));
            workflowRun.setDetectedIntent(classifyResponse.detected_intent());

            // 2. Extract
            ExtractResponse extractResponse = aiServiceClient.extract(new ExtractRequest(
                    workflowRun.getInputContent(),
                    workflowRun.getDetectedIntent()
            ));
            try {
                workflowRun.setExtractedFieldsJson(objectMapper.writeValueAsString(extractResponse.fields()));
            } catch (JsonProcessingException e) {
                log.error("Failed to serialize extracted fields", e);
            }

            // 3. Generate Action
            GenerateActionResponse generateResponse = aiServiceClient.generate(new GenerateActionRequest(
                    "Recommend the next action for this " + workflowRun.getDetectedIntent(),
                    extractResponse.fields()
            ));
            workflowRun.setRecommendedAction(generateResponse.content());

            workflowRun.setStatus(RunStatus.COMPLETED);
        } catch (Exception e) {
            log.error("AI processing failed for workflow run: {}", workflowRun.getId(), e);
            workflowRun.setStatus(RunStatus.FAILED);
        }

        return toResponse(workflowRunRepository.save(workflowRun));
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
