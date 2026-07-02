package com.deploypilot.deployment;

import com.deploypilot.common.AuditableEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;
import java.util.UUID;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "deployment_configs")
@Getter
@Setter
@NoArgsConstructor
public class DeploymentConfig extends AuditableEntity {

    @Column(nullable = false)
    private UUID customerId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private DeploymentEnvironment environment;

    @Column(nullable = false)
    private String modelName;

    @Column(nullable = false)
    private boolean llmEnabled;

    @Column(nullable = false)
    private boolean approvalRequired;

    @Column(nullable = false)
    private double confidenceThreshold;

    @Column(nullable = false)
    private int maxMonthlyRuns;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private DeploymentConfigStatus status = DeploymentConfigStatus.ACTIVE;
}
