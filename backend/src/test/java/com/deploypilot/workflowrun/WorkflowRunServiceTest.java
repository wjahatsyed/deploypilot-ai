package com.deploypilot.workflowrun;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.deploypilot.common.AuditableEntity;
import com.deploypilot.customer.Customer;
import com.deploypilot.deployment.DeploymentConfig;
import com.deploypilot.deployment.DeploymentConfigRepository;
import com.deploypilot.deployment.DeploymentConfigStatus;
import com.deploypilot.deployment.DeploymentEnvironment;
import com.deploypilot.workflow.Workflow;
import com.deploypilot.workflow.WorkflowService;
import com.deploypilot.workflow.WorkflowStatus;
import com.deploypilot.ai.AiServiceClient;
import com.deploypilot.ai.ClassifyRequest;
import com.deploypilot.ai.ClassifyResponse;
import com.deploypilot.ai.ExtractResponse;
import com.deploypilot.ai.GenerateActionResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.lang.reflect.Field;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class WorkflowRunServiceTest {

    @Mock
    private WorkflowRunRepository workflowRunRepository;

    @Mock
    private WorkflowService workflowService;

    @Mock
    private DeploymentConfigRepository deploymentConfigRepository;

    @Mock
    private AiServiceClient aiServiceClient;

    @Spy
    private ObjectMapper objectMapper = new ObjectMapper();

    @InjectMocks
    private WorkflowRunService workflowRunService;

    @Test
    void createAttachesWorkflowAndProcessesRun() {
        UUID workflowId = UUID.randomUUID();
        UUID runId = UUID.randomUUID();
        Workflow workflow = workflow(workflowId);
        when(workflowService.getEntityById(workflowId)).thenReturn(workflow);
        when(workflowRunRepository.save(any(WorkflowRun.class))).thenAnswer(invocation -> {
            WorkflowRun run = invocation.getArgument(0);
            if (run.getId() == null) {
                setAuditFields(run, runId);
            }
            return run;
        });

        when(aiServiceClient.classify(any())).thenReturn(new ClassifyResponse("deployment_request", 0.9, "mock"));
        when(aiServiceClient.extract(any())).thenReturn(new ExtractResponse(Map.of("env", "prod"), "mock"));
        when(aiServiceClient.generate(any())).thenReturn(new GenerateActionResponse("Deploy now", "mock"));

        WorkflowRunResponse response = workflowRunService.create(workflowId, new CreateWorkflowRunRequest(
                "email",
                "Please deploy version 1.2.3"
        ));

        assertThat(response.id()).isEqualTo(runId);
        assertThat(response.workflowId()).isEqualTo(workflowId);
        assertThat(response.status()).isEqualTo(RunStatus.COMPLETED);
        assertThat(response.detectedIntent()).isEqualTo("deployment_request");
        assertThat(response.recommendedAction()).isEqualTo("Deploy now");
        verify(workflowService).getEntityById(workflowId);
    }

    @Test
    void createMarksRunFailedWhenAiServiceFails() {
        UUID workflowId = UUID.randomUUID();
        UUID runId = UUID.randomUUID();
        Workflow workflow = workflow(workflowId);
        when(workflowService.getEntityById(workflowId)).thenReturn(workflow);
        when(workflowRunRepository.save(any(WorkflowRun.class))).thenAnswer(invocation -> {
            WorkflowRun run = invocation.getArgument(0);
            if (run.getId() == null) {
                setAuditFields(run, runId);
            }
            return run;
        });

        when(aiServiceClient.classify(any())).thenThrow(new RuntimeException("AI down"));

        WorkflowRunResponse response = workflowRunService.create(workflowId, new CreateWorkflowRunRequest(
                "email",
                "Please deploy version 1.2.3"
        ));

        assertThat(response.status()).isEqualTo(RunStatus.FAILED);
    }

    @Test
    void createRespectsApprovalRequiredConfig() {
        UUID workflowId = UUID.randomUUID();
        Workflow workflow = workflow(workflowId);
        UUID customerId = workflow.getCustomer().getId();

        DeploymentConfig config = new DeploymentConfig();
        config.setCustomerId(customerId);
        config.setEnvironment(DeploymentEnvironment.PROD);
        config.setLlmEnabled(true);
        config.setApprovalRequired(true);
        config.setConfidenceThreshold(0.5);

        when(workflowService.getEntityById(workflowId)).thenReturn(workflow);
        when(deploymentConfigRepository.findByCustomerIdAndEnvironmentAndStatus(
                customerId, DeploymentEnvironment.PROD, DeploymentConfigStatus.ACTIVE))
                .thenReturn(Optional.of(config));
        when(workflowRunRepository.save(any(WorkflowRun.class))).thenAnswer(invocation -> invocation.getArgument(0));

        when(aiServiceClient.classify(any())).thenReturn(new ClassifyResponse("intent", 0.9, "mock"));
        when(aiServiceClient.extract(any())).thenReturn(new ExtractResponse(Map.of(), "mock"));
        when(aiServiceClient.generate(any())).thenReturn(new GenerateActionResponse("action", "mock"));

        WorkflowRunResponse response = workflowRunService.create(workflowId, new CreateWorkflowRunRequest("api", "content"));

        assertThat(response.status()).isEqualTo(RunStatus.WAITING_FOR_APPROVAL);
    }

    @Test
    void createRespectsConfidenceThresholdConfig() {
        UUID workflowId = UUID.randomUUID();
        Workflow workflow = workflow(workflowId);
        UUID customerId = workflow.getCustomer().getId();

        DeploymentConfig config = new DeploymentConfig();
        config.setCustomerId(customerId);
        config.setEnvironment(DeploymentEnvironment.PROD);
        config.setLlmEnabled(true);
        config.setApprovalRequired(false);
        config.setConfidenceThreshold(0.95);

        when(workflowService.getEntityById(workflowId)).thenReturn(workflow);
        when(deploymentConfigRepository.findByCustomerIdAndEnvironmentAndStatus(
                customerId, DeploymentEnvironment.PROD, DeploymentConfigStatus.ACTIVE))
                .thenReturn(Optional.of(config));
        when(workflowRunRepository.save(any(WorkflowRun.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // Confidence 0.9 is below threshold 0.95
        when(aiServiceClient.classify(any())).thenReturn(new ClassifyResponse("intent", 0.9, "mock"));
        when(aiServiceClient.extract(any())).thenReturn(new ExtractResponse(Map.of(), "mock"));
        when(aiServiceClient.generate(any())).thenReturn(new GenerateActionResponse("action", "mock"));

        WorkflowRunResponse response = workflowRunService.create(workflowId, new CreateWorkflowRunRequest("api", "content"));

        assertThat(response.status()).isEqualTo(RunStatus.WAITING_FOR_APPROVAL);
    }

    @Test
    void createUsesMockWhenLlmDisabled() {
        UUID workflowId = UUID.randomUUID();
        Workflow workflow = workflow(workflowId);
        UUID customerId = workflow.getCustomer().getId();

        DeploymentConfig config = new DeploymentConfig();
        config.setCustomerId(customerId);
        config.setEnvironment(DeploymentEnvironment.PROD);
        config.setLlmEnabled(false);

        when(workflowService.getEntityById(workflowId)).thenReturn(workflow);
        when(deploymentConfigRepository.findByCustomerIdAndEnvironmentAndStatus(
                customerId, DeploymentEnvironment.PROD, DeploymentConfigStatus.ACTIVE))
                .thenReturn(Optional.of(config));
        when(workflowRunRepository.save(any(WorkflowRun.class))).thenAnswer(invocation -> invocation.getArgument(0));

        WorkflowRunResponse response = workflowRunService.create(workflowId, new CreateWorkflowRunRequest("api", "content"));

        assertThat(response.status()).isEqualTo(RunStatus.COMPLETED);
        assertThat(response.detectedIntent()).isEqualTo("MOCK_INTENT");
    }

    @Test
    void findByWorkflowIdReturnsRunsForWorkflow() {
        UUID workflowId = UUID.randomUUID();
        UUID runId = UUID.randomUUID();
        Workflow workflow = workflow(workflowId);
        WorkflowRun run = new WorkflowRun();
        run.setWorkflow(workflow);
        run.setInputSource("api");
        run.setInputContent("deploy service");
        run.setStatus(RunStatus.PROCESSING);
        setAuditFields(run, runId);
        when(workflowService.getEntityById(workflowId)).thenReturn(workflow);
        when(workflowRunRepository.findByWorkflowId(workflowId)).thenReturn(List.of(run));

        List<WorkflowRunResponse> responses = workflowRunService.findByWorkflowId(workflowId);

        assertThat(responses).hasSize(1);
        assertThat(responses.getFirst().id()).isEqualTo(runId);
        assertThat(responses.getFirst().status()).isEqualTo(RunStatus.PROCESSING);
        verify(workflowService).getEntityById(workflowId);
    }

    private static Workflow workflow(UUID workflowId) {
        Customer customer = new Customer();
        customer.setName("Acme");
        setAuditFields(customer, UUID.randomUUID());

        Workflow workflow = new Workflow();
        workflow.setCustomer(customer);
        workflow.setName("Deploy workflow");
        workflow.setStatus(WorkflowStatus.ACTIVE);
        setAuditFields(workflow, workflowId);
        return workflow;
    }

    private static void setAuditFields(Object entity, UUID id) {
        try {
            setField(entity, "id", id);
            setField(entity, "createdAt", Instant.now());
            setField(entity, "updatedAt", Instant.now());
        } catch (ReflectiveOperationException ex) {
            throw new IllegalStateException(ex);
        }
    }

    private static void setField(Object entity, String fieldName, Object value)
            throws ReflectiveOperationException {
        Field field = AuditableEntity.class.getDeclaredField(fieldName);
        field.setAccessible(true);
        field.set(entity, value);
    }
}
