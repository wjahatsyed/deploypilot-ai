package com.deploypilot.evaluation;

import com.deploypilot.common.AuditableEntity;
import com.deploypilot.workflowrun.WorkflowRun;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "evaluation_results")
@Getter
@Setter
@NoArgsConstructor
public class EvaluationResult extends AuditableEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "workflow_run_id", nullable = false)
    private WorkflowRun workflowRun;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private EvaluationStatus status = EvaluationStatus.PENDING;

    private Double score;

    @Column(nullable = false)
    private boolean passed;

    private boolean intentMatched;

    private Double fieldAccuracy;

    private Double actionQualityScore;

    @Column(columnDefinition = "text")
    private String failureReasonsJson;

    private Long latencyMs;

    @Column(columnDefinition = "text")
    private String summary;
}
