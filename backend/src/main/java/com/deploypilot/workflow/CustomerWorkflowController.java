package com.deploypilot.workflow;

import java.util.List;
import java.util.UUID;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/customers/{customerId}/workflows")
public class CustomerWorkflowController {

    private final WorkflowService workflowService;

    public CustomerWorkflowController(WorkflowService workflowService) {
        this.workflowService = workflowService;
    }

    @GetMapping
    public List<WorkflowResponse> findByCustomerId(@PathVariable UUID customerId) {
        return workflowService.findByCustomerId(customerId);
    }
}
