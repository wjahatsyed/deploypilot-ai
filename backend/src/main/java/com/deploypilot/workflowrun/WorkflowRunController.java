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
@RequestMapping("/api/workflow-runs")
public class WorkflowRunController {

    private final WorkflowRunService workflowRunService;

    public WorkflowRunController(WorkflowRunService workflowRunService) {
        this.workflowRunService = workflowRunService;
    }

    @GetMapping
    public List<WorkflowRun> findAll() {
        return workflowRunService.findAll();
    }

    @GetMapping("/{id}")
    public ResponseEntity<WorkflowRun> findById(@PathVariable UUID id) {
        return workflowRunService.findById(id)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<WorkflowRun> create(@Valid @RequestBody WorkflowRun workflowRun) {
        WorkflowRun created = workflowRunService.create(workflowRun);
        return ResponseEntity.created(URI.create("/api/workflow-runs/" + created.getId())).body(created);
    }
}
