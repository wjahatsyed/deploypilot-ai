package com.deploypilot.approval;

import java.time.Instant;
import java.util.UUID;

public record HumanApprovalResponse(
        UUID id,
        UUID workflowRunId,
        ApprovalStatus status,
        String requestedBy,
        String reviewedBy,
        Instant reviewedAt,
        String notes,
        Instant createdAt,
        Instant updatedAt
) {

    public static HumanApprovalResponse from(HumanApproval humanApproval) {
        return new HumanApprovalResponse(
                humanApproval.getId(),
                humanApproval.getWorkflowRun().getId(),
                humanApproval.getStatus(),
                humanApproval.getRequestedBy(),
                humanApproval.getReviewedBy(),
                humanApproval.getReviewedAt(),
                humanApproval.getNotes(),
                humanApproval.getCreatedAt(),
                humanApproval.getUpdatedAt()
        );
    }
}
