package com.deploypilot.approval;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.deploypilot.audit.AuditLog;
import com.deploypilot.audit.AuditLogRepository;
import com.deploypilot.common.exception.ResourceNotFoundException;
import com.deploypilot.workflowrun.RunStatus;
import com.deploypilot.workflowrun.WorkflowRun;
import com.deploypilot.workflowrun.WorkflowRunRepository;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class ApprovalServiceTest {

    @Mock
    private HumanApprovalRepository humanApprovalRepository;

    @Mock
    private WorkflowRunRepository workflowRunRepository;

    @Mock
    private AuditLogRepository auditLogRepository;

    @InjectMocks
    private ApprovalService approvalService;

    @Test
    void findPendingReturnsOnlyPendingApprovals() {
        HumanApproval pending = new HumanApproval();
        pending.setStatus(ApprovalStatus.PENDING);
        
        when(humanApprovalRepository.findByStatus(ApprovalStatus.PENDING)).thenReturn(List.of(pending));

        List<HumanApproval> result = approvalService.findPending();

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getStatus()).isEqualTo(ApprovalStatus.PENDING);
    }

    @Test
    void approveChangesStatusToCompletedAndApproved() {
        UUID runId = UUID.randomUUID();
        WorkflowRun run = new WorkflowRun();
        run.setStatus(RunStatus.WAITING_FOR_APPROVAL);
        
        HumanApproval approval = new HumanApproval();
        approval.setWorkflowRun(run);
        approval.setStatus(ApprovalStatus.PENDING);

        when(humanApprovalRepository.findByWorkflowRunId(runId)).thenReturn(Optional.of(approval));
        when(workflowRunRepository.save(any())).thenAnswer(i -> i.getArgument(0));
        when(humanApprovalRepository.save(any())).thenAnswer(i -> i.getArgument(0));

        HumanApproval result = approvalService.approve(runId, "reviewer@example.com", "Looks good");

        assertThat(result.getStatus()).isEqualTo(ApprovalStatus.APPROVED);
        assertThat(result.getReviewerEmail()).isEqualTo("reviewer@example.com");
        assertThat(result.getReviewerComment()).isEqualTo("Looks good");
        assertThat(run.getStatus()).isEqualTo(RunStatus.COMPLETED);
        
        verify(auditLogRepository).save(any(AuditLog.class));
    }

    @Test
    void rejectChangesStatusToRejected() {
        UUID runId = UUID.randomUUID();
        WorkflowRun run = new WorkflowRun();
        run.setStatus(RunStatus.WAITING_FOR_APPROVAL);
        
        HumanApproval approval = new HumanApproval();
        approval.setWorkflowRun(run);
        approval.setStatus(ApprovalStatus.PENDING);

        when(humanApprovalRepository.findByWorkflowRunId(runId)).thenReturn(Optional.of(approval));
        when(workflowRunRepository.save(any())).thenAnswer(i -> i.getArgument(0));
        when(humanApprovalRepository.save(any())).thenAnswer(i -> i.getArgument(0));

        HumanApproval result = approvalService.reject(runId, "reviewer@example.com", "Bad request");

        assertThat(result.getStatus()).isEqualTo(ApprovalStatus.REJECTED);
        assertThat(run.getStatus()).isEqualTo(RunStatus.REJECTED);
        
        verify(auditLogRepository).save(any(AuditLog.class));
    }

    @Test
    void approveThrowsExceptionIfNoPendingApprovalFound() {
        UUID runId = UUID.randomUUID();
        when(humanApprovalRepository.findByWorkflowRunId(runId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> approvalService.approve(runId, "test@test.com", "msg"))
                .isInstanceOf(ResourceNotFoundException.class);
    }
}
