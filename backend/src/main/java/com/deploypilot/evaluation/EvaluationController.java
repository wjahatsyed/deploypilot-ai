package com.deploypilot.evaluation;

import jakarta.validation.Valid;
import java.net.URI;
import java.util.List;
import java.util.UUID;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/evaluations")
public class EvaluationController {

    private final EvaluationService evaluationService;

    public EvaluationController(EvaluationService evaluationService) {
        this.evaluationService = evaluationService;
    }

    @GetMapping
    public List<EvaluationResult> findAll() {
        return evaluationService.findAll();
    }

    @GetMapping("/{id}")
    public ResponseEntity<EvaluationResult> findById(@PathVariable UUID id) {
        return evaluationService.findById(id)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<EvaluationResult> create(@Valid @RequestBody EvaluationResult evaluationResult) {
        EvaluationResult created = evaluationService.create(evaluationResult);
        return ResponseEntity.created(URI.create("/api/evaluations/" + created.getId())).body(created);
    }
}
