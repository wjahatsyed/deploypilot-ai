package com.deploypilot.approval;

import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface HumanApprovalRepository extends JpaRepository<HumanApproval, UUID> {
}
