package com.deploypilot.ai;

import com.fasterxml.jackson.annotation.JsonProperty;

public record ClassifyResponse(
        @JsonProperty("detected_intent") String detectedIntent,
        double confidence,
        @JsonProperty("model_name") String modelName
) {}
