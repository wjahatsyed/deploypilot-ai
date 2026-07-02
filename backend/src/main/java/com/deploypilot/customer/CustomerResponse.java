package com.deploypilot.customer;

import java.time.Instant;
import java.util.UUID;

public record CustomerResponse(
        UUID id,
        String name,
        String industry,
        String region,
        String contactEmail,
        Instant createdAt,
        Instant updatedAt
) {
}
