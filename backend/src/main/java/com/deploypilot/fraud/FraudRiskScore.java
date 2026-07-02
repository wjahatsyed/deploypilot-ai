package com.deploypilot.fraud;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FraudRiskScore {
    private double score;
    private boolean flagged;
    private String reason;
}
