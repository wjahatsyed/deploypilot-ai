package com.deploypilot.evaluation;

import com.deploypilot.common.AuditableEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import java.util.UUID;

@Entity
@Table(name = "evaluation_datasets")
@Getter
@Setter
@NoArgsConstructor
public class EvaluationDataset extends AuditableEntity {

    @Column(nullable = false)
    private UUID customerId;

    @Column(nullable = false)
    private String name;

    @Column(columnDefinition = "text")
    private String description;
}
