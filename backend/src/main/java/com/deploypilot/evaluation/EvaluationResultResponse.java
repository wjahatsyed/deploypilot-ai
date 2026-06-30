package com.deploypilot.evaluation;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record EvaluationResultResponse(
        UUID id,
        UUID workflowRunId,
        EvaluationStatus status,
        BigDecimal score,
        String summary,
        Instant createdAt,
        Instant updatedAt
) {

    public static EvaluationResultResponse from(EvaluationResult evaluationResult) {
        return new EvaluationResultResponse(
                evaluationResult.getId(),
                evaluationResult.getWorkflowRun().getId(),
                evaluationResult.getStatus(),
                evaluationResult.getScore(),
                evaluationResult.getSummary(),
                evaluationResult.getCreatedAt(),
                evaluationResult.getUpdatedAt()
        );
    }
}
