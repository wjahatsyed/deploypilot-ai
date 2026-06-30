package com.deploypilot.workflowrun;

import jakarta.validation.constraints.NotBlank;

public record CreateWorkflowRunRequest(
        String inputSource,
        @NotBlank(message = "Workflow run inputContent is required")
        String inputContent
) {
}
