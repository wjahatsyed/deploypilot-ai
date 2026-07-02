package com.deploypilot.evaluation;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.UUID;

public interface EvaluationCaseRepository extends JpaRepository<EvaluationCase, UUID> {
    List<EvaluationCase> findByDatasetId(UUID datasetId);
}
