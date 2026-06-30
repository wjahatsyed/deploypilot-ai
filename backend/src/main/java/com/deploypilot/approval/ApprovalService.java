package com.deploypilot.approval;

import com.deploypilot.audit.AuditLog;
import com.deploypilot.audit.AuditLogRepository;
import com.deploypilot.common.exception.ResourceNotFoundException;
import com.deploypilot.workflowrun.RunStatus;
import com.deploypilot.workflowrun.WorkflowRun;
import com.deploypilot.workflowrun.WorkflowRunRepository;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class ApprovalService {

    private final HumanApprovalRepository humanApprovalRepository;
    private final WorkflowRunRepository workflowRunRepository;
    private final AuditLogRepository auditLogRepository;

    public ApprovalService(
            HumanApprovalRepository humanApprovalRepository,
            WorkflowRunRepository workflowRunRepository,
            AuditLogRepository auditLogRepository
    ) {
        this.humanApprovalRepository = humanApprovalRepository;
        this.workflowRunRepository = workflowRunRepository;
        this.auditLogRepository = auditLogRepository;
    }

    public List<HumanApproval> findAll() {
        return humanApprovalRepository.findAll();
    }

    public List<HumanApproval> findPending() {
        return humanApprovalRepository.findByStatus(ApprovalStatus.PENDING);
    }

    public Optional<HumanApproval> findById(UUID id) {
        return humanApprovalRepository.findById(id);
    }

    public Optional<HumanApproval> findByWorkflowRunId(UUID workflowRunId) {
        return humanApprovalRepository.findByWorkflowRunId(workflowRunId);
    }

    @Transactional
    public HumanApproval create(HumanApproval humanApproval) {
        return humanApprovalRepository.save(humanApproval);
    }

    @Transactional
    public HumanApproval approve(UUID runId, String email, String comment) {
        HumanApproval approval = getPendingApproval(runId);
        approval.setStatus(ApprovalStatus.APPROVED);
        approval.setReviewerEmail(email);
        approval.setReviewerComment(comment);

        WorkflowRun run = approval.getWorkflowRun();
        run.setStatus(RunStatus.COMPLETED);
        workflowRunRepository.save(run);

        createAuditLog("APPROVE", runId, email, comment);

        return humanApprovalRepository.save(approval);
    }

    @Transactional
    public HumanApproval reject(UUID runId, String email, String comment) {
        HumanApproval approval = getPendingApproval(runId);
        approval.setStatus(ApprovalStatus.REJECTED);
        approval.setReviewerEmail(email);
        approval.setReviewerComment(comment);

        WorkflowRun run = approval.getWorkflowRun();
        run.setStatus(RunStatus.REJECTED);
        workflowRunRepository.save(run);

        createAuditLog("REJECT", runId, email, comment);

        return humanApprovalRepository.save(approval);
    }

    private HumanApproval getPendingApproval(UUID runId) {
        return humanApprovalRepository.findByWorkflowRunId(runId)
                .filter(a -> a.getStatus() == ApprovalStatus.PENDING)
                .orElseThrow(() -> new ResourceNotFoundException("Pending approval not found for run: " + runId));
    }

    private void createAuditLog(String action, UUID runId, String actor, String comment) {
        AuditLog log = new AuditLog();
        log.setActor(actor);
        log.setAction(action);
        log.setEntityType("WorkflowRun");
        log.setEntityId(runId);
        log.setDetails(comment);
        auditLogRepository.save(log);
    }
}
