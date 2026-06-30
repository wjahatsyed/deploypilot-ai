package com.deploypilot.evaluation;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class EvaluationService {

    private final EvaluationResultRepository evaluationResultRepository;

    public EvaluationService(EvaluationResultRepository evaluationResultRepository) {
        this.evaluationResultRepository = evaluationResultRepository;
    }

    public List<EvaluationResult> findAll() {
        return evaluationResultRepository.findAll();
    }

    public Optional<EvaluationResult> findById(UUID id) {
        return evaluationResultRepository.findById(id);
    }

    @Transactional
    public EvaluationResult create(EvaluationResult evaluationResult) {
        return evaluationResultRepository.save(evaluationResult);
    }
}
