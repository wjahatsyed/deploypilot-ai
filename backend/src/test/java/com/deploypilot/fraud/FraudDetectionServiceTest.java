package com.deploypilot.fraud;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import static org.junit.jupiter.api.Assertions.*;

class FraudDetectionServiceTest {

    private FraudDetectionService fraudDetectionService;

    @BeforeEach
    void setUp() {
        fraudDetectionService = new FraudDetectionService();
        ReflectionTestUtils.setField(fraudDetectionService, "riskThreshold", 0.8);
    }

    @Test
    void analyzeRequest_WhenSafe_ReturnsLowScore() {
        FraudRiskScore result = fraudDetectionService.analyzeRequest("CLI", "Deploy version 1.0");
        
        assertFalse(result.isFlagged());
        assertEquals(0.0, result.getScore());
        assertEquals("Request appears safe", result.getReason());
    }

    @Test
    void analyzeRequest_WhenSuspiciousSource_IncreasesScore() {
        FraudRiskScore result = fraudDetectionService.analyzeRequest("anonymous", "Deploy version 1.0");
        
        assertFalse(result.isFlagged()); // 0.5 < 0.8
        assertEquals(0.5, result.getScore());
    }

    @Test
    void analyzeRequest_WhenDestructiveContent_FlagsFraud() {
        FraudRiskScore result = fraudDetectionService.analyzeRequest("CLI", "drop table users;");
        
        assertTrue(result.isFlagged()); // 0.9 >= 0.8
        assertEquals(0.9, result.getScore());
        assertTrue(result.getReason().contains("Destructive commands detected"));
    }

    @Test
    void analyzeRequest_WhenMultipleHeuristics_AggregatesScore() {
        FraudRiskScore result = fraudDetectionService.analyzeRequest("anonymous", "a".repeat(1001));
        
        assertFalse(result.isFlagged()); // 0.5 + 0.2 = 0.7 < 0.8
        assertEquals(0.7, result.getScore());
    }
}
