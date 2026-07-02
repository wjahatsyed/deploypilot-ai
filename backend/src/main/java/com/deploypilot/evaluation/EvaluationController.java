package com.deploypilot.evaluation;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.net.URI;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping
@RequiredArgsConstructor
public class EvaluationController {

    private final EvaluationService evaluationService;

    @PostMapping("/api/eval-datasets")
    public ResponseEntity<EvaluationDatasetResponse> createDataset(@Valid @RequestBody EvaluationDatasetRequest request) {
        EvaluationDataset dataset = evaluationService.createDataset(request);
        return ResponseEntity.created(URI.create("/api/eval-datasets/" + dataset.getId()))
                .body(EvaluationDatasetResponse.from(dataset));
    }

    @PostMapping("/api/eval-datasets/{datasetId}/cases")
    public ResponseEntity<EvaluationCase> createCase(@PathVariable UUID datasetId, @Valid @RequestBody EvaluationCaseRequest request) {
        EvaluationCase evalCase = evaluationService.createCase(datasetId, request);
        return ResponseEntity.created(URI.create("/api/eval-datasets/" + datasetId + "/cases/" + evalCase.getId()))
                .body(evalCase);
    }

    @GetMapping("/api/eval-datasets")
    public List<EvaluationDatasetResponse> findAllDatasets() {
        return evaluationService.findAllDatasets().stream()
                .map(EvaluationDatasetResponse::from)
                .toList();
    }

    @PostMapping("/api/eval-datasets/{datasetId}/run")
    public ResponseEntity<EvaluationSummary> runEvaluation(@PathVariable UUID datasetId) {
        EvaluationSummary summary = evaluationService.runEvaluation(datasetId);
        return ResponseEntity.ok(summary);
    }

    @GetMapping("/api/evals/summary")
    public EvaluationSummary getSummary() {
        return evaluationService.getSummary();
    }
}
