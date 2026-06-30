package com.deploypilot.evaluation;

import com.deploypilot.ai.AiServiceClient;
import com.deploypilot.ai.ClassifyRequest;
import com.deploypilot.ai.ClassifyResponse;
import com.deploypilot.ai.ExtractRequest;
import com.deploypilot.ai.ExtractResponse;
import com.deploypilot.ai.GenerateActionRequest;
import com.deploypilot.ai.GenerateActionResponse;
import com.deploypilot.workflow.Workflow;
import com.deploypilot.workflow.WorkflowRepository;
import com.deploypilot.workflowrun.RunStatus;
import com.deploypilot.workflowrun.WorkflowRun;
import com.deploypilot.workflowrun.WorkflowRunRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
@Slf4j
public class EvaluationService {

    private final EvaluationDatasetRepository evaluationDatasetRepository;
    private final EvaluationCaseRepository evaluationCaseRepository;
    private final EvaluationResultRepository evaluationResultRepository;
    private final AiServiceClient aiServiceClient;
    private final WorkflowRepository workflowRepository;
    private final WorkflowRunRepository workflowRunRepository;

    public List<EvaluationDataset> findAllDatasets() {
        return evaluationDatasetRepository.findAll();
    }

    public Optional<EvaluationDataset> findDatasetById(UUID id) {
        return evaluationDatasetRepository.findById(id);
    }

    @Transactional
    public EvaluationDataset createDataset(EvaluationDatasetRequest request) {
        EvaluationDataset dataset = new EvaluationDataset();
        dataset.setCustomerId(request.customerId());
        dataset.setName(request.name());
        dataset.setDescription(request.description());
        return evaluationDatasetRepository.save(dataset);
    }

    @Transactional
    public EvaluationCase createCase(UUID datasetId, EvaluationCaseRequest request) {
        EvaluationCase evalCase = new EvaluationCase();
        evalCase.setDatasetId(datasetId);
        evalCase.setInputContent(request.inputContent());
        evalCase.setExpectedIntent(request.expectedIntent());
        evalCase.setExpectedFieldsJson(request.expectedFieldsJson());
        evalCase.setExpectedActionKeywords(request.expectedActionKeywords());
        return evaluationCaseRepository.save(evalCase);
    }

    @Transactional
    public EvaluationSummary runEvaluation(UUID datasetId) {
        EvaluationDataset dataset = evaluationDatasetRepository.findById(datasetId)
                .orElseThrow(() -> new RuntimeException("Dataset not found"));

        List<EvaluationCase> cases = evaluationCaseRepository.findByDatasetId(datasetId);
        if (cases.isEmpty()) {
            return new EvaluationSummary(0, 0, 0, 0);
        }

        List<EvaluationResult> results = new ArrayList<>();
        
        // Find or create a dummy workflow for evaluation runs if needed
        // For simplicity, we'll just create a workflow run for each case
        Workflow workflow = workflowRepository.findAll().stream().findFirst()
                .orElseThrow(() -> new RuntimeException("No workflow found to run evaluation against"));

        for (EvaluationCase evalCase : cases) {
            long startTime = System.currentTimeMillis();
            
            // 1. Classify
            ClassifyResponse classifyResp = aiServiceClient.classify(new ClassifyRequest(evalCase.getInputContent(), "eval", workflow.getId().toString()));
            
            // 2. Extract
            ExtractResponse extractResp = aiServiceClient.extract(new ExtractRequest(evalCase.getInputContent(), evalCase.getExpectedIntent()));
            
            // 3. Generate
            GenerateActionResponse generateResp = aiServiceClient.generate(new GenerateActionRequest(
                    evalCase.getExpectedIntent(), extractResp.fields()));

            long latency = System.currentTimeMillis() - startTime;

            // Save WorkflowRun
            WorkflowRun run = new WorkflowRun();
            run.setWorkflow(workflow);
            run.setStatus(RunStatus.COMPLETED);
            run.setInputContent(evalCase.getInputContent());
            run.setDetectedIntent(classifyResp.detectedIntent());
            run.setExtractedFieldsJson("{}"); // Should serialize extractResp.fields()
            run.setRecommendedAction(generateResp.content());
            workflowRunRepository.save(run);

            // Evaluate
            EvaluationResult result = new EvaluationResult();
            result.setWorkflowRun(run);
            result.setLatencyMs(latency);
            
            boolean intentMatched = evalCase.getExpectedIntent().equalsIgnoreCase(classifyResp.detectedIntent());
            result.setIntentMatched(intentMatched);
            
            // Simple scoring logic
            double score = intentMatched ? 1.0 : 0.0;
            // In a real app, compare fields and action keywords for more granular score
            
            result.setScore(score);
            result.setPassed(score >= 0.8);
            result.setStatus(result.isPassed() ? EvaluationStatus.PASSED : EvaluationStatus.FAILED);
            
            evaluationResultRepository.save(result);
            results.add(result);
        }

        long total = results.size();
        long passed = results.stream().filter(EvaluationResult::isPassed).count();
        double avgScore = results.stream().mapToDouble(EvaluationResult::getScore).average().orElse(0.0);
        
        return new EvaluationSummary(total, passed, (double) passed / total, avgScore);
    }

    public EvaluationSummary getSummary() {
        List<EvaluationResult> results = evaluationResultRepository.findAll();
        if (results.isEmpty()) {
            return new EvaluationSummary(0, 0, 0, 0);
        }
        long total = results.size();
        long passed = results.stream().filter(EvaluationResult::isPassed).count();
        double avgScore = results.stream().mapToDouble(EvaluationResult::getScore).average().orElse(0.0);
        
        return new EvaluationSummary(total, passed, (double) passed / total, avgScore);
    }
}
