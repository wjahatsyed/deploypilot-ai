package com.deploypilot.ai;

import com.fasterxml.jackson.annotation.JsonProperty;

public record ExtractRequest(
        @JsonProperty("input_content") String inputContent,
        @JsonProperty("detected_intent") String detectedIntent
) {}
