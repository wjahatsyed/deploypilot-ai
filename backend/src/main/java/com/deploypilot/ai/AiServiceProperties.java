package com.deploypilot.ai;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import lombok.Getter;
import lombok.Setter;

/**
 * Configuration properties for the AI service.
 */
@Configuration
@ConfigurationProperties(prefix = "ai.service")
@Getter
@Setter
public class AiServiceProperties {
    private String baseUrl = "http://localhost:8000";
    private int timeoutMs = 5000;
}
