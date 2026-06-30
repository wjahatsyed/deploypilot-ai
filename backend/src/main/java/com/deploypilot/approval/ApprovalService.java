package com.deploypilot.approval;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class ApprovalService {

    private final HumanApprovalRepository humanApprovalRepository;

    public ApprovalService(HumanApprovalRepository humanApprovalRepository) {
        this.humanApprovalRepository = humanApprovalRepository;
    }

    public List<HumanApproval> findAll() {
        return humanApprovalRepository.findAll();
    }

    public Optional<HumanApproval> findById(UUID id) {
        return humanApprovalRepository.findById(id);
    }

    @Transactional
    public HumanApproval create(HumanApproval humanApproval) {
        return humanApprovalRepository.save(humanApproval);
    }
}
