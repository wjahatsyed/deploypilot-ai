package com.deploypilot.customer;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record CreateCustomerRequest(
        @NotBlank(message = "Customer name is required")
        String name,
        String industry,
        String region,
        @Email(message = "Contact email must be valid")
        String contactEmail
) {
}
