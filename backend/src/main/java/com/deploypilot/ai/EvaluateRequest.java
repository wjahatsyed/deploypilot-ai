package com.deploypilot.ai;

import com.fasterxml.jackson.annotation.JsonProperty;

public record EvaluateRequest(
        @JsonProperty("input_content") String inputContent,
        @JsonProperty("detected_intent") String detectedIntent,
        @JsonProperty("recommended_action") String recommendedAction
) {}
