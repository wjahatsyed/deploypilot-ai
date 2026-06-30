package com.deploypilot.ai;

import java.util.Map;

public record ClassifyRequest(String input_content, String input_source, String workflow_id) {}
public record ClassifyResponse(String detected_intent, double confidence, String model_name) {}

public record ExtractRequest(String input_content, String detected_intent) {}
public record ExtractResponse(Map<String, Object> fields, String model_name) {}

public record GenerateActionRequest(String prompt, Map<String, Object> context) {}
public record GenerateActionResponse(String content, String model_name) {}

public record EvaluateRequest(String input_content, String detected_intent, String recommended_action) {}
public record EvaluateResponse(double score, String feedback, String model_name) {}
