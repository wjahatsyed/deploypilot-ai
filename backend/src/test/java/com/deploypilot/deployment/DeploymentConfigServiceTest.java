package com.deploypilot.deployment;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.deploypilot.common.AuditableEntity;
import java.lang.reflect.Field;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class DeploymentConfigServiceTest {

    @Mock
    private DeploymentConfigRepository deploymentConfigRepository;

    @InjectMocks
    private DeploymentConfigService deploymentConfigService;

    @Test
    void createSavesNewConfig() {
        UUID customerId = UUID.randomUUID();
        CreateDeploymentConfigRequest request = new CreateDeploymentConfigRequest(
                DeploymentEnvironment.PROD,
                "gpt-4",
                true,
                true,
                0.85,
                1000
        );

        when(deploymentConfigRepository.save(any(DeploymentConfig.class))).thenAnswer(invocation -> {
            DeploymentConfig config = invocation.getArgument(0);
            setAuditFields(config, UUID.randomUUID());
            return config;
        });

        DeploymentConfigResponse response = deploymentConfigService.create(customerId, request);

        assertThat(response.customerId()).isEqualTo(customerId);
        assertThat(response.environment()).isEqualTo(DeploymentEnvironment.PROD);
        assertThat(response.modelName()).isEqualTo("gpt-4");
        assertThat(response.approvalRequired()).isTrue();
        assertThat(response.confidenceThreshold()).isEqualTo(0.85);
        verify(deploymentConfigRepository).save(any(DeploymentConfig.class));
    }

    @Test
    void findByCustomerIdReturnsConfigs() {
        UUID customerId = UUID.randomUUID();
        DeploymentConfig config = new DeploymentConfig();
        config.setCustomerId(customerId);
        config.setEnvironment(DeploymentEnvironment.DEV);
        config.setModelName("gpt-3.5");
        setAuditFields(config, UUID.randomUUID());

        when(deploymentConfigRepository.findByCustomerId(customerId)).thenReturn(List.of(config));

        List<DeploymentConfigResponse> responses = deploymentConfigService.findByCustomerId(customerId);

        assertThat(responses).hasSize(1);
        assertThat(responses.get(0).customerId()).isEqualTo(customerId);
        assertThat(responses.get(0).environment()).isEqualTo(DeploymentEnvironment.DEV);
    }

    @Test
    void updateModifiesExistingConfig() {
        UUID configId = UUID.randomUUID();
        DeploymentConfig config = new DeploymentConfig();
        config.setCustomerId(UUID.randomUUID());
        config.setEnvironment(DeploymentEnvironment.PROD);
        setAuditFields(config, configId);

        UpdateDeploymentConfigRequest request = new UpdateDeploymentConfigRequest(
                "claude-3",
                false,
                false,
                0.5,
                500,
                DeploymentConfigStatus.DISABLED
        );

        when(deploymentConfigRepository.findById(configId)).thenReturn(Optional.of(config));
        when(deploymentConfigRepository.save(any(DeploymentConfig.class))).thenReturn(config);

        DeploymentConfigResponse response = deploymentConfigService.update(configId, request);

        assertThat(response.modelName()).isEqualTo("claude-3");
        assertThat(response.llmEnabled()).isFalse();
        assertThat(response.status()).isEqualTo(DeploymentConfigStatus.DISABLED);
    }

    private static void setAuditFields(Object entity, UUID id) {
        try {
            setField(entity, "id", id);
            setField(entity, "createdAt", Instant.now());
            setField(entity, "updatedAt", Instant.now());
        } catch (ReflectiveOperationException ex) {
            throw new IllegalStateException(ex);
        }
    }

    private static void setField(Object entity, String fieldName, Object value)
            throws ReflectiveOperationException {
        Field field = AuditableEntity.class.getDeclaredField(fieldName);
        field.setAccessible(true);
        field.set(entity, value);
    }
}
