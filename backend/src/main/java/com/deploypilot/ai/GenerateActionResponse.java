package com.deploypilot.ai;

import com.fasterxml.jackson.annotation.JsonProperty;

public record GenerateActionResponse(
        String content,
        @JsonProperty("model_name") String modelName
) {}
