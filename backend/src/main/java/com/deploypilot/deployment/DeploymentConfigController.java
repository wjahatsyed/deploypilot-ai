package com.deploypilot.deployment;

import jakarta.validation.Valid;
import java.util.List;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
public class DeploymentConfigController {

    private final DeploymentConfigService deploymentConfigService;

    public DeploymentConfigController(DeploymentConfigService deploymentConfigService) {
        this.deploymentConfigService = deploymentConfigService;
    }

    @PostMapping("/customers/{customerId}/deployment-configs")
    @ResponseStatus(HttpStatus.CREATED)
    public DeploymentConfigResponse create(
            @PathVariable UUID customerId,
            @Valid @RequestBody CreateDeploymentConfigRequest request
    ) {
        return deploymentConfigService.create(customerId, request);
    }

    @GetMapping("/customers/{customerId}/deployment-configs")
    public List<DeploymentConfigResponse> findByCustomerId(@PathVariable UUID customerId) {
        return deploymentConfigService.findByCustomerId(customerId);
    }

    @GetMapping("/deployment-configs/{id}")
    public DeploymentConfigResponse findById(@PathVariable UUID id) {
        return deploymentConfigService.findById(id);
    }

    @PutMapping("/deployment-configs/{id}")
    public DeploymentConfigResponse update(
            @PathVariable UUID id,
            @Valid @RequestBody UpdateDeploymentConfigRequest request
    ) {
        return deploymentConfigService.update(id, request);
    }
}
