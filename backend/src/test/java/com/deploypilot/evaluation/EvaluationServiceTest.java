package com.deploypilot.evaluation;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import com.deploypilot.ai.AiServiceClient;
import com.deploypilot.ai.ClassifyRequest;
import com.deploypilot.ai.ClassifyResponse;
import com.deploypilot.ai.ExtractRequest;
import com.deploypilot.ai.ExtractResponse;
import com.deploypilot.ai.GenerateActionRequest;
import com.deploypilot.ai.GenerateActionResponse;
import com.deploypilot.common.AuditableEntity;
import com.deploypilot.workflow.Workflow;
import com.deploypilot.workflow.WorkflowRepository;
import com.deploypilot.workflowrun.WorkflowRun;
import com.deploypilot.workflowrun.WorkflowRunRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.lang.reflect.Field;
import java.time.Instant;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@ExtendWith(MockitoExtension.class)
class EvaluationServiceTest {

    @Mock
    private EvaluationDatasetRepository evaluationDatasetRepository;
    @Mock
    private EvaluationCaseRepository evaluationCaseRepository;
    @Mock
    private EvaluationResultRepository evaluationResultRepository;
    @Mock
    private AiServiceClient aiServiceClient;
    @Mock
    private WorkflowRepository workflowRepository;
    @Mock
    private WorkflowRunRepository workflowRunRepository;

    @InjectMocks
    private EvaluationService evaluationService;

    @Test
    void createDatasetReturnsSavedDataset() {
        UUID customerId = UUID.randomUUID();
        EvaluationDatasetRequest request = new EvaluationDatasetRequest(customerId, "Test Dataset", "Description");
        
        when(evaluationDatasetRepository.save(any(EvaluationDataset.class))).thenAnswer(invocation -> {
            EvaluationDataset ds = invocation.getArgument(0);
            setAuditFields(ds, UUID.randomUUID());
            return ds;
        });

        EvaluationDataset result = evaluationService.createDataset(request);

        assertThat(result.getName()).isEqualTo("Test Dataset");
        assertThat(result.getCustomerId()).isEqualTo(customerId);
        verify(evaluationDatasetRepository).save(any(EvaluationDataset.class));
    }

    @Test
    void runEvaluationWithMockedAiReturnsSummary() {
        UUID datasetId = UUID.randomUUID();
        EvaluationDataset dataset = new EvaluationDataset();
        setAuditFields(dataset, datasetId);
        
        EvaluationCase evalCase = new EvaluationCase();
        evalCase.setDatasetId(datasetId);
        evalCase.setInputContent("Deploy service X");
        evalCase.setExpectedIntent("DEPLOY");
        setAuditFields(evalCase, UUID.randomUUID());

        Workflow workflow = new Workflow();
        setAuditFields(workflow, UUID.randomUUID());

        when(evaluationDatasetRepository.findById(datasetId)).thenReturn(Optional.of(dataset));
        when(evaluationCaseRepository.findByDatasetId(datasetId)).thenReturn(List.of(evalCase));
        when(workflowRepository.findAll()).thenReturn(List.of(workflow));

        when(aiServiceClient.classify(any(ClassifyRequest.class))).thenReturn(new ClassifyResponse("DEPLOY", 0.9, "model"));
        when(aiServiceClient.extract(any(ExtractRequest.class))).thenReturn(new ExtractResponse(Map.of("service", "X"), "model"));
        when(aiServiceClient.generate(any(GenerateActionRequest.class))).thenReturn(new GenerateActionResponse("Deploying X", "model"));

        EvaluationSummary summary = evaluationService.runEvaluation(datasetId);

        assertThat(summary.totalCases()).isEqualTo(1);
        assertThat(summary.passedCases()).isEqualTo(1);
        assertThat(summary.passRate()).isEqualTo(1.0);
        assertThat(summary.averageScore()).isEqualTo(1.0);

        verify(evaluationResultRepository).save(any(EvaluationResult.class));
        verify(workflowRunRepository).save(any(WorkflowRun.class));
    }

    private static void setAuditFields(AuditableEntity entity, UUID id) {
        try {
            setField(entity, "id", id);
            setField(entity, "createdAt", Instant.now());
            setField(entity, "updatedAt", Instant.now());
        } catch (ReflectiveOperationException ex) {
            throw new IllegalStateException(ex);
        }
    }

    private static void setField(AuditableEntity entity, String fieldName, Object value)
            throws ReflectiveOperationException {
        Field field = AuditableEntity.class.getDeclaredField(fieldName);
        field.setAccessible(true);
        field.set(entity, value);
    }
}
