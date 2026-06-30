package com.deploypilot.workflow;

import com.deploypilot.common.exception.ResourceNotFoundException;
import com.deploypilot.customer.Customer;
import com.deploypilot.customer.CustomerService;
import java.util.List;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class WorkflowService {

    private final WorkflowRepository workflowRepository;
    private final CustomerService customerService;

    public WorkflowService(WorkflowRepository workflowRepository, CustomerService customerService) {
        this.workflowRepository = workflowRepository;
        this.customerService = customerService;
    }

    public List<WorkflowResponse> findAll() {
        return workflowRepository.findAll().stream()
                .map(WorkflowService::toResponse)
                .toList();
    }

    public WorkflowResponse findById(UUID id) {
        return workflowRepository.findById(id)
                .map(WorkflowService::toResponse)
                .orElseThrow(() -> new ResourceNotFoundException("Workflow not found: " + id));
    }

    public List<WorkflowResponse> findByCustomerId(UUID customerId) {
        customerService.getEntityById(customerId);
        return workflowRepository.findByCustomerId(customerId).stream()
                .map(WorkflowService::toResponse)
                .toList();
    }

    @Transactional
    public WorkflowResponse create(CreateWorkflowRequest request) {
        Customer customer = customerService.getEntityById(request.customerId());

        Workflow workflow = new Workflow();
        workflow.setCustomer(customer);
        workflow.setName(request.name());
        workflow.setDescription(request.description());
        workflow.setStatus(WorkflowStatus.DRAFT);

        return toResponse(workflowRepository.save(workflow));
    }

    public Workflow getEntityById(UUID id) {
        return workflowRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Workflow not found: " + id));
    }

    static WorkflowResponse toResponse(Workflow workflow) {
        return new WorkflowResponse(
                workflow.getId(),
                workflow.getCustomer().getId(),
                workflow.getName(),
                workflow.getDescription(),
                workflow.getStatus(),
                workflow.getCreatedAt(),
                workflow.getUpdatedAt()
        );
    }
}
