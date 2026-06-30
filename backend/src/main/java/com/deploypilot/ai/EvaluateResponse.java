package com.deploypilot.ai;

import com.fasterxml.jackson.annotation.JsonProperty;

public record EvaluateResponse(
        double score,
        String feedback,
        @JsonProperty("model_name") String modelName
) {}
