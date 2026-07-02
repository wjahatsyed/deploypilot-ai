package com.deploypilot.evaluation;

import java.time.Instant;
import java.util.UUID;

public record EvaluationResultResponse(
        UUID id,
        UUID workflowRunId,
        EvaluationStatus status,
        Double score,
        boolean passed,
        boolean intentMatched,
        Double fieldAccuracy,
        Double actionQualityScore,
        String failureReasonsJson,
        Long latencyMs,
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
                evaluationResult.isPassed(),
                evaluationResult.isIntentMatched(),
                evaluationResult.getFieldAccuracy(),
                evaluationResult.getActionQualityScore(),
                evaluationResult.getFailureReasonsJson(),
                evaluationResult.getLatencyMs(),
                evaluationResult.getSummary(),
                evaluationResult.getCreatedAt(),
                evaluationResult.getUpdatedAt()
        );
    }
}
