package com.deploypilot.approval;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface HumanApprovalRepository extends JpaRepository<HumanApproval, UUID> {
    Optional<HumanApproval> findByWorkflowRunId(UUID workflowRunId);
    List<HumanApproval> findByStatus(ApprovalStatus status);
}
