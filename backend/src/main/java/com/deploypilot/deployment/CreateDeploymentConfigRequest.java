package com.deploypilot.deployment;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.util.UUID;

public record CreateDeploymentConfigRequest(
        @NotNull DeploymentEnvironment environment,
        @NotBlank String modelName,
        boolean llmEnabled,
        boolean approvalRequired,
        @DecimalMin("0.0") @DecimalMax("1.0") double confidenceThreshold,
        int maxMonthlyRuns
) {}
