package com.deploypilot.workflow;

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
@RequestMapping("/api/workflows")
public class WorkflowController {

    private final WorkflowService workflowService;

    public WorkflowController(WorkflowService workflowService) {
        this.workflowService = workflowService;
    }

    @GetMapping
    public List<Workflow> findAll() {
        return workflowService.findAll();
    }

    @GetMapping("/{id}")
    public ResponseEntity<Workflow> findById(@PathVariable UUID id) {
        return workflowService.findById(id)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<Workflow> create(@Valid @RequestBody Workflow workflow) {
        Workflow created = workflowService.create(workflow);
        return ResponseEntity.created(URI.create("/api/workflows/" + created.getId())).body(created);
    }
}
