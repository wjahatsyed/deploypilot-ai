package com.deploypilot.approval;

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
@RequestMapping("/api/approvals")
public class ApprovalController {

    private final ApprovalService approvalService;

    public ApprovalController(ApprovalService approvalService) {
        this.approvalService = approvalService;
    }

    @GetMapping
    public List<HumanApproval> findAll() {
        return approvalService.findAll();
    }

    @GetMapping("/{id}")
    public ResponseEntity<HumanApproval> findById(@PathVariable UUID id) {
        return approvalService.findById(id)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<HumanApproval> create(@Valid @RequestBody HumanApproval humanApproval) {
        HumanApproval created = approvalService.create(humanApproval);
        return ResponseEntity.created(URI.create("/api/approvals/" + created.getId())).body(created);
    }
}
