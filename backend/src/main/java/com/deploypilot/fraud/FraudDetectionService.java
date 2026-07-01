package com.deploypilot.fraud;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class FraudDetectionService {

    @Value("${fraud.detection.threshold:0.8}")
    private double riskThreshold;

    public FraudRiskScore analyzeRequest(String inputSource, String inputContent) {
        log.info("Analyzing request for fraud. Source: {}, Content length: {}", inputSource, inputContent.length());
        
        double score = 0.0;
        StringBuilder reason = new StringBuilder();

        // Heuristic 1: Suspicious source
        if ("anonymous".equalsIgnoreCase(inputSource) || "unknown".equalsIgnoreCase(inputSource)) {
            score += 0.5;
            reason.append("Suspicious input source; ");
        }

        // Heuristic 2: Critical keywords in content
        String lowerContent = inputContent.toLowerCase();
        if (lowerContent.contains("drop table") || lowerContent.contains("delete all") || lowerContent.contains("rm -rf")) {
            score += 0.9;
            reason.append("Destructive commands detected; ");
        }

        // Heuristic 3: Excessive length
        if (inputContent.length() > 1000) {
            score += 0.2;
            reason.append("Excessive input length; ");
        }

        boolean flagged = score >= riskThreshold;
        
        return FraudRiskScore.builder()
                .score(Math.min(score, 1.0))
                .flagged(flagged)
                .reason(flagged ? reason.toString().trim() : "Request appears safe")
                .build();
    }
}
