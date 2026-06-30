package com.deploypilot.evaluation;

import java.time.Instant;
import java.util.UUID;

public record EvaluationDatasetResponse(
    UUID id,
    UUID customerId,
    String name,
    String description,
    Instant createdAt,
    Instant updatedAt
) {
    public static EvaluationDatasetResponse from(EvaluationDataset dataset) {
        return new EvaluationDatasetResponse(
            dataset.getId(),
            dataset.getCustomerId(),
            dataset.getName(),
            dataset.getDescription(),
            dataset.getCreatedAt(),
            dataset.getUpdatedAt()
        );
    }
}
