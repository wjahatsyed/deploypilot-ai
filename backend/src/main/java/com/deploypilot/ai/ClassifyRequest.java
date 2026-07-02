package com.deploypilot.ai;

import com.fasterxml.jackson.annotation.JsonProperty;

public record ClassifyRequest(
        @JsonProperty("input_content") String inputContent,
        @JsonProperty("input_source") String inputSource,
        @JsonProperty("workflow_id") String workflowId
) {}
