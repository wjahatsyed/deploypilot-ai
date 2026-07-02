package com.deploypilot.evaluation;

import java.util.UUID;

public record EvaluationCaseRequest(
    String inputContent,
    String expectedIntent,
    String expectedFieldsJson,
    String expectedActionKeywords
) {}
