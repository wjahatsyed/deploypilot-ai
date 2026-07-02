package com.deploypilot.workflow;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.deploypilot.common.AuditableEntity;
import com.deploypilot.customer.Customer;
import com.deploypilot.customer.CustomerService;
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
class WorkflowServiceTest {

    @Mock
    private WorkflowRepository workflowRepository;

    @Mock
    private CustomerService customerService;

    @InjectMocks
    private WorkflowService workflowService;

    @Test
    void createAttachesCustomerAndReturnsDraftWorkflow() {
        UUID customerId = UUID.randomUUID();
        UUID workflowId = UUID.randomUUID();
        Customer customer = customer(customerId);
        when(customerService.getEntityById(customerId)).thenReturn(customer);
        when(workflowRepository.save(any(Workflow.class))).thenAnswer(invocation -> {
            Workflow workflow = invocation.getArgument(0);
            setAuditFields(workflow, workflowId);
            return workflow;
        });

        WorkflowResponse response = workflowService.create(new CreateWorkflowRequest(
                customerId,
                "Invoice triage",
                "Route invoice requests"
        ));

        assertThat(response.id()).isEqualTo(workflowId);
        assertThat(response.customerId()).isEqualTo(customerId);
        assertThat(response.name()).isEqualTo("Invoice triage");
        assertThat(response.status()).isEqualTo(WorkflowStatus.DRAFT);
        verify(customerService).getEntityById(customerId);
    }

    @Test
    void findByCustomerIdValidatesCustomerAndReturnsWorkflows() {
        UUID customerId = UUID.randomUUID();
        UUID workflowId = UUID.randomUUID();
        Customer customer = customer(customerId);
        Workflow workflow = new Workflow();
        workflow.setCustomer(customer);
        workflow.setName("Invoice triage");
        workflow.setStatus(WorkflowStatus.ACTIVE);
        setAuditFields(workflow, workflowId);
        when(customerService.getEntityById(customerId)).thenReturn(customer);
        when(workflowRepository.findByCustomerId(customerId)).thenReturn(List.of(workflow));

        List<WorkflowResponse> responses = workflowService.findByCustomerId(customerId);

        assertThat(responses).hasSize(1);
        assertThat(responses.getFirst().id()).isEqualTo(workflowId);
        verify(customerService).getEntityById(customerId);
    }

    private static Customer customer(UUID customerId) {
        Customer customer = new Customer();
        customer.setName("Acme");
        setAuditFields(customer, customerId);
        return customer;
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
