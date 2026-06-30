package com.deploypilot.evaluation;

import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface EvaluationResultRepository extends JpaRepository<EvaluationResult, UUID> {
}
