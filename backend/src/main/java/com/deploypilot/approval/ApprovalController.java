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
@RequestMapping("/api")
public class ApprovalController {

    private final ApprovalService approvalService;

    public ApprovalController(ApprovalService approvalService) {
        this.approvalService = approvalService;
    }

    @GetMapping("/approvals/pending")
    public List<HumanApprovalResponse> findPending() {
        return approvalService.findPending().stream()
                .map(HumanApprovalResponse::from)
                .toList();
    }

    @GetMapping("/runs/{runId}/approval")
    public ResponseEntity<HumanApprovalResponse> findByRunId(@PathVariable UUID runId) {
        return approvalService.findByWorkflowRunId(runId)
                .map(HumanApprovalResponse::from)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @PostMapping("/runs/{runId}/approve")
    public HumanApprovalResponse approve(@PathVariable UUID runId, @Valid @RequestBody ApprovalRequest request) {
        return HumanApprovalResponse.from(approvalService.approve(runId, request.email(), request.comment()));
    }

    @PostMapping("/runs/{runId}/reject")
    public HumanApprovalResponse reject(@PathVariable UUID runId, @Valid @RequestBody ApprovalRequest request) {
        return HumanApprovalResponse.from(approvalService.reject(runId, request.email(), request.comment()));
    }
}
