package com.deploypilot.workflowrun;

import jakarta.validation.Valid;
import java.net.URI;
import java.util.List;
import java.util.UUID;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
public class WorkflowRunController {

    private final WorkflowRunService workflowRunService;

    public WorkflowRunController(WorkflowRunService workflowRunService) {
        this.workflowRunService = workflowRunService;
    }

    @PostMapping("/workflows/{workflowId}/runs")
    public ResponseEntity<WorkflowRunResponse> create(
            @PathVariable UUID workflowId,
            @Valid @RequestBody CreateWorkflowRunRequest request
    ) {
        WorkflowRunResponse created = workflowRunService.create(workflowId, request);
        return ResponseEntity.created(URI.create("/api/runs/" + created.id())).body(created);
    }

    @GetMapping("/workflows/{workflowId}/runs")
    public List<WorkflowRunResponse> findByWorkflowId(@PathVariable UUID workflowId) {
        return workflowRunService.findByWorkflowId(workflowId);
    }

    @GetMapping("/runs/{runId}")
    public WorkflowRunResponse findById(@PathVariable UUID runId) {
        return workflowRunService.findById(runId);
    }
}
