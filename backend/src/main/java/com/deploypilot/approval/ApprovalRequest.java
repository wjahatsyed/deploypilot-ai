package com.deploypilot.approval;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record ApprovalRequest(
        @NotBlank @Email String email,
        String comment
) {}
