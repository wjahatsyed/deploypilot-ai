package com.deploypilot.deployment;

import com.deploypilot.common.exception.ResourceNotFoundException;
import java.util.List;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class DeploymentConfigService {

    private final DeploymentConfigRepository deploymentConfigRepository;

    public DeploymentConfigService(DeploymentConfigRepository deploymentConfigRepository) {
        this.deploymentConfigRepository = deploymentConfigRepository;
    }

    public List<DeploymentConfigResponse> findByCustomerId(UUID customerId) {
        return deploymentConfigRepository.findByCustomerId(customerId).stream()
                .map(this::toResponse)
                .toList();
    }

    public DeploymentConfigResponse findById(UUID id) {
        return deploymentConfigRepository.findById(id)
                .map(this::toResponse)
                .orElseThrow(() -> new ResourceNotFoundException("Deployment config not found: " + id));
    }

    @Transactional
    public DeploymentConfigResponse create(UUID customerId, CreateDeploymentConfigRequest request) {
        DeploymentConfig config = new DeploymentConfig();
        config.setCustomerId(customerId);
        config.setEnvironment(request.environment());
        config.setModelName(request.modelName());
        config.setLlmEnabled(request.llmEnabled());
        config.setApprovalRequired(request.approvalRequired());
        config.setConfidenceThreshold(request.confidenceThreshold());
        config.setMaxMonthlyRuns(request.maxMonthlyRuns());
        
        return toResponse(deploymentConfigRepository.save(config));
    }

    @Transactional
    public DeploymentConfigResponse update(UUID id, UpdateDeploymentConfigRequest request) {
        DeploymentConfig config = deploymentConfigRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Deployment config not found: " + id));

        config.setModelName(request.modelName());
        config.setLlmEnabled(request.llmEnabled());
        config.setApprovalRequired(request.approvalRequired());
        config.setConfidenceThreshold(request.confidenceThreshold());
        config.setMaxMonthlyRuns(request.maxMonthlyRuns());
        config.setStatus(request.status());

        return toResponse(deploymentConfigRepository.save(config));
    }

    public DeploymentConfigResponse toResponse(DeploymentConfig config) {
        return new DeploymentConfigResponse(
                config.getId(),
                config.getCustomerId(),
                config.getEnvironment(),
                config.getModelName(),
                config.isLlmEnabled(),
                config.isApprovalRequired(),
                config.getConfidenceThreshold(),
                config.getMaxMonthlyRuns(),
                config.getStatus(),
                config.getCreatedAt(),
                config.getUpdatedAt()
        );
    }
}
