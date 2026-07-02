package com.deploypilot.approval;

import java.time.Instant;
import java.util.UUID;

public record HumanApprovalResponse(
        UUID id,
        UUID workflowRunId,
        ApprovalStatus status,
        String reviewerEmail,
        String reviewerComment,
        String requestedBy,
        Instant reviewedAt,
        Instant createdAt,
        Instant updatedAt
) {

    public static HumanApprovalResponse from(HumanApproval humanApproval) {
        return new HumanApprovalResponse(
                humanApproval.getId(),
                humanApproval.getWorkflowRun().getId(),
                humanApproval.getStatus(),
                humanApproval.getReviewerEmail(),
                humanApproval.getReviewerComment(),
                humanApproval.getRequestedBy(),
                humanApproval.getReviewedAt(),
                humanApproval.getCreatedAt(),
                humanApproval.getUpdatedAt()
        );
    }
}
