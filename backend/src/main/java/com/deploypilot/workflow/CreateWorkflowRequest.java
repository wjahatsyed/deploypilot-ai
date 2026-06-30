package com.deploypilot.workflow;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.util.UUID;

public record CreateWorkflowRequest(
        @NotNull(message = "Workflow customerId is required")
        UUID customerId,
        @NotBlank(message = "Workflow name is required")
        String name,
        String description
) {
}
