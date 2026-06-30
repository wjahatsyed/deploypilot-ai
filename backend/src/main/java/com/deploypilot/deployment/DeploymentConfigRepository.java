package com.deploypilot.deployment;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface DeploymentConfigRepository extends JpaRepository<DeploymentConfig, UUID> {
    List<DeploymentConfig> findByCustomerId(UUID customerId);

    Optional<DeploymentConfig> findByCustomerIdAndEnvironmentAndStatus(
            UUID customerId, DeploymentEnvironment environment, DeploymentConfigStatus status);
}
