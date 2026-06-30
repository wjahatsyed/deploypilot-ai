package com.deploypilot.workflowrun;

import java.time.Instant;
import java.util.UUID;

public record WorkflowRunResponse(
        UUID id,
        UUID workflowId,
        String inputSource,
        String inputContent,
        String detectedIntent,
        String extractedFieldsJson,
        String recommendedAction,
        RunStatus status,
        Instant createdAt,
        Instant updatedAt
) {
}
