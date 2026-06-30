package com.deploypilot.workflowrun;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class WorkflowRunService {

    private final WorkflowRunRepository workflowRunRepository;

    public WorkflowRunService(WorkflowRunRepository workflowRunRepository) {
        this.workflowRunRepository = workflowRunRepository;
    }

    public List<WorkflowRun> findAll() {
        return workflowRunRepository.findAll();
    }

    public Optional<WorkflowRun> findById(UUID id) {
        return workflowRunRepository.findById(id);
    }

    @Transactional
    public WorkflowRun create(WorkflowRun workflowRun) {
        return workflowRunRepository.save(workflowRun);
    }
}
