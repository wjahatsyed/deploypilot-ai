package com.deploypilot.workflow;

import java.time.Instant;
import java.util.UUID;

public record WorkflowResponse(
        UUID id,
        UUID customerId,
        String name,
        String description,
        WorkflowStatus status,
        Instant createdAt,
        Instant updatedAt
) {
}
