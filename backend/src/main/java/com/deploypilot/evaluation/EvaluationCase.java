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
@Table(name = "evaluation_cases")
@Getter
@Setter
@NoArgsConstructor
public class EvaluationCase extends AuditableEntity {

    @Column(nullable = false)
    private UUID datasetId;

    @Column(nullable = false, columnDefinition = "text")
    private String inputContent;

    @Column(nullable = false)
    private String expectedIntent;

    @Column(columnDefinition = "text")
    private String expectedFieldsJson;

    @Column(columnDefinition = "text")
    private String expectedActionKeywords;
}
