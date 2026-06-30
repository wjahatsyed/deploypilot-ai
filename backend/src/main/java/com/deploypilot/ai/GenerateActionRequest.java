package com.deploypilot.ai;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.Map;

public record GenerateActionRequest(
        String prompt,
        Map<String, Object> context
) {}
