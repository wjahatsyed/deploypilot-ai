package com.deploypilot.evaluation;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.UUID;

public interface EvaluationDatasetRepository extends JpaRepository<EvaluationDataset, UUID> {
}
