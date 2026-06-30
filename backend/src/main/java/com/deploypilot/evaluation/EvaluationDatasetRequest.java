package com.deploypilot.evaluation;

import java.util.UUID;

public record EvaluationDatasetRequest(
    UUID customerId,
    String name,
    String description
) {}
