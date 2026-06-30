package com.deploypilot.workflowrun;

import com.deploypilot.common.exception.ResourceNotFoundException;
import com.deploypilot.workflow.Workflow;
import com.deploypilot.workflow.WorkflowService;
import java.util.List;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class WorkflowRunService {

    private final WorkflowRunRepository workflowRunRepository;
    private final WorkflowService workflowService;

    public WorkflowRunService(
            WorkflowRunRepository workflowRunRepository,
            WorkflowService workflowService
    ) {
        this.workflowRunRepository = workflowRunRepository;
        this.workflowService = workflowService;
    }

    public List<WorkflowRunResponse> findByWorkflowId(UUID workflowId) {
        workflowService.getEntityById(workflowId);
        return workflowRunRepository.findByWorkflowId(workflowId).stream()
                .map(WorkflowRunService::toResponse)
                .toList();
    }

    public WorkflowRunResponse findById(UUID id) {
        return workflowRunRepository.findById(id)
                .map(WorkflowRunService::toResponse)
                .orElseThrow(() -> new ResourceNotFoundException("Workflow run not found: " + id));
    }

    @Transactional
    public WorkflowRunResponse create(UUID workflowId, CreateWorkflowRunRequest request) {
        Workflow workflow = workflowService.getEntityById(workflowId);

        WorkflowRun workflowRun = new WorkflowRun();
        workflowRun.setWorkflow(workflow);
        workflowRun.setInputSource(request.inputSource());
        workflowRun.setInputContent(request.inputContent());
        workflowRun.setStatus(RunStatus.QUEUED);

        return toResponse(workflowRunRepository.save(workflowRun));
    }

    static WorkflowRunResponse toResponse(WorkflowRun workflowRun) {
        return new WorkflowRunResponse(
                workflowRun.getId(),
                workflowRun.getWorkflow().getId(),
                workflowRun.getInputSource(),
                workflowRun.getInputContent(),
                workflowRun.getDetectedIntent(),
                workflowRun.getExtractedFieldsJson(),
                workflowRun.getRecommendedAction(),
                workflowRun.getStatus(),
                workflowRun.getCreatedAt(),
                workflowRun.getUpdatedAt()
        );
    }
}
