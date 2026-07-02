package com.deploypilot.ai;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.Map;

public record ExtractResponse(
        Map<String, Object> fields,
        @JsonProperty("model_name") String modelName
) {}
