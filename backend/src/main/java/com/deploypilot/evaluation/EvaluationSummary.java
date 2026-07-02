package com.deploypilot.evaluation;

public record EvaluationSummary(
    long totalCases,
    long passedCases,
    double passRate,
    double averageScore
) {}
