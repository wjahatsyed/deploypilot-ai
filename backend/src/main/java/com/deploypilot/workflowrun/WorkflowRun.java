package com.deploypilot.workflowrun;

import com.deploypilot.common.AuditableEntity;
import com.deploypilot.workflow.Workflow;
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
@Table(name = "workflow_runs")
@Getter
@Setter
@NoArgsConstructor
public class WorkflowRun extends AuditableEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "workflow_id", nullable = false)
    private Workflow workflow;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private RunStatus status = RunStatus.QUEUED;

    private String inputSource;

    @Column(nullable = false, columnDefinition = "text")
    private String inputContent;

    private String detectedIntent;

    @Column(columnDefinition = "text")
    private String extractedFieldsJson;

    @Column(columnDefinition = "text")
    private String recommendedAction;
}
