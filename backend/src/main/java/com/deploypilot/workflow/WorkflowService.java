package com.deploypilot.workflow;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class WorkflowService {

    private final WorkflowRepository workflowRepository;

    public WorkflowService(WorkflowRepository workflowRepository) {
        this.workflowRepository = workflowRepository;
    }

    public List<Workflow> findAll() {
        return workflowRepository.findAll();
    }

    public Optional<Workflow> findById(UUID id) {
        return workflowRepository.findById(id);
    }

    @Transactional
    public Workflow create(Workflow workflow) {
        return workflowRepository.save(workflow);
    }
}
