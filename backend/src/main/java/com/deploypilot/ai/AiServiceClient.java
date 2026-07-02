package com.deploypilot.ai;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClient;

import java.util.Map;

/**
 * HTTP client for communicating with the Python FastAPI AI service.
 */
@Service
public class AiServiceClient {
    private static final Logger log = LoggerFactory.getLogger(AiServiceClient.class);
    private final RestClient restClient;

    public AiServiceClient(AiServiceProperties properties) {
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(properties.getTimeoutMs());
        factory.setReadTimeout(properties.getTimeoutMs());

        this.restClient = RestClient.builder()
                .baseUrl(properties.getBaseUrl())
                .requestFactory(factory)
                .defaultStatusHandler(HttpStatusCode::isError, (request, response) -> {
                    throw new AiServiceException("AI service error: " + response.getStatusCode());
                })
                .build();
    }

    public ClassifyResponse classify(ClassifyRequest request) {
        log.info("Calling AI service /classify");
        try {
            return restClient.post()
                    .uri("/classify")
                    .body(request)
                    .retrieve()
                    .body(ClassifyResponse.class);
        } catch (ResourceAccessException e) {
            throw new AiServiceException("AI service unavailable or timeout", e);
        }
    }

    public ExtractResponse extract(ExtractRequest request) {
        log.info("Calling AI service /extract");
        try {
            return restClient.post()
                    .uri("/extract")
                    .body(request)
                    .retrieve()
                    .body(ExtractResponse.class);
        } catch (ResourceAccessException e) {
            throw new AiServiceException("AI service unavailable or timeout", e);
        }
    }

    public GenerateActionResponse generate(GenerateActionRequest request) {
        log.info("Calling AI service /generate");
        try {
            return restClient.post()
                    .uri("/generate")
                    .body(request)
                    .retrieve()
                    .body(GenerateActionResponse.class);
        } catch (ResourceAccessException e) {
            throw new AiServiceException("AI service unavailable or timeout", e);
        }
    }

    public EvaluateResponse evaluate(EvaluateRequest request) {
        log.info("Calling AI service /evaluate");
        try {
            return restClient.post()
                    .uri("/evaluate")
                    .body(request)
                    .retrieve()
                    .body(EvaluateResponse.class);
        } catch (ResourceAccessException e) {
            throw new AiServiceException("AI service unavailable or timeout", e);
        }
    }
}
