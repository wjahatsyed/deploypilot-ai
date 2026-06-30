package com.deploypilot.deployment;

import java.time.Instant;
import java.util.UUID;

public record DeploymentConfigResponse(
        UUID id,
        UUID customerId,
        DeploymentEnvironment environment,
        String modelName,
        boolean llmEnabled,
        boolean approvalRequired,
        double confidenceThreshold,
        int maxMonthlyRuns,
        DeploymentConfigStatus status,
        Instant createdAt,
        Instant updatedAt
) {}
