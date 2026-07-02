package com.deploypilot.evaluation;

import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface EvaluationDatasetRepository extends JpaRepository<EvaluationDataset, UUID> {
    Optional<EvaluationDataset> findByCustomerIdAndName(UUID customerId, String name);
}
