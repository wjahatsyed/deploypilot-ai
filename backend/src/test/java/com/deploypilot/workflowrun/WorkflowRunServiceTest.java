package com.deploypilot.workflowrun;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.deploypilot.common.AuditableEntity;
import com.deploypilot.customer.Customer;
import com.deploypilot.workflow.Workflow;
import com.deploypilot.workflow.WorkflowService;
import com.deploypilot.workflow.WorkflowStatus;
import java.lang.reflect.Field;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class WorkflowRunServiceTest {

    @Mock
    private WorkflowRunRepository workflowRunRepository;

    @Mock
    private WorkflowService workflowService;

    @InjectMocks
    private WorkflowRunService workflowRunService;

    @Test
    void createAttachesWorkflowAndQueuesRun() {
        UUID workflowId = UUID.randomUUID();
        UUID runId = UUID.randomUUID();
        Workflow workflow = workflow(workflowId);
        when(workflowService.getEntityById(workflowId)).thenReturn(workflow);
        when(workflowRunRepository.save(any(WorkflowRun.class))).thenAnswer(invocation -> {
            WorkflowRun run = invocation.getArgument(0);
            setAuditFields(run, runId);
            return run;
        });

        WorkflowRunResponse response = workflowRunService.create(workflowId, new CreateWorkflowRunRequest(
                "email",
                "Please deploy version 1.2.3"
        ));

        assertThat(response.id()).isEqualTo(runId);
        assertThat(response.workflowId()).isEqualTo(workflowId);
        assertThat(response.inputSource()).isEqualTo("email");
        assertThat(response.inputContent()).isEqualTo("Please deploy version 1.2.3");
        assertThat(response.status()).isEqualTo(RunStatus.QUEUED);
        verify(workflowService).getEntityById(workflowId);
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
        run.setStatus(RunStatus.RUNNING);
        setAuditFields(run, runId);
        when(workflowService.getEntityById(workflowId)).thenReturn(workflow);
        when(workflowRunRepository.findByWorkflowId(workflowId)).thenReturn(List.of(run));

        List<WorkflowRunResponse> responses = workflowRunService.findByWorkflowId(workflowId);

        assertThat(responses).hasSize(1);
        assertThat(responses.getFirst().id()).isEqualTo(runId);
        assertThat(responses.getFirst().status()).isEqualTo(RunStatus.RUNNING);
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

    private static void setAuditFields(AuditableEntity entity, UUID id) {
        try {
            setField(entity, "id", id);
            setField(entity, "createdAt", Instant.now());
            setField(entity, "updatedAt", Instant.now());
        } catch (ReflectiveOperationException ex) {
            throw new IllegalStateException(ex);
        }
    }

    private static void setField(AuditableEntity entity, String fieldName, Object value)
            throws ReflectiveOperationException {
        Field field = AuditableEntity.class.getDeclaredField(fieldName);
        field.setAccessible(true);
        field.set(entity, value);
    }
}
