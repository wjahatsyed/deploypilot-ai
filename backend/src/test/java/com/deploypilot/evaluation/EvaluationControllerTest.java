package com.deploypilot.evaluation;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@WebMvcTest(EvaluationController.class)
class EvaluationControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private EvaluationService evaluationService;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void createDatasetReturnsCreated() throws Exception {
        UUID customerId = UUID.randomUUID();
        UUID datasetId = UUID.randomUUID();
        EvaluationDatasetRequest request = new EvaluationDatasetRequest(customerId, "New Dataset", "Desc");
        
        EvaluationDataset dataset = new EvaluationDataset();
        dataset.setCustomerId(customerId);
        dataset.setName("New Dataset");
        setField(dataset, "id", datasetId);
        setField(dataset, "createdAt", Instant.now());
        setField(dataset, "updatedAt", Instant.now());

        when(evaluationService.createDataset(any(EvaluationDatasetRequest.class))).thenReturn(dataset);

        mockMvc.perform(post("/api/eval-datasets")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(datasetId.toString()))
                .andExpect(jsonPath("$.name").value("New Dataset"));
    }

    @Test
    void getSummaryReturnsData() throws Exception {
        EvaluationSummary summary = new EvaluationSummary(10, 8, 0.8, 0.85);
        when(evaluationService.getSummary()).thenReturn(summary);

        mockMvc.perform(get("/api/evals/summary"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalCases").value(10))
                .andExpect(jsonPath("$.passedCases").value(8))
                .andExpect(jsonPath("$.passRate").value(0.8));
    }

    private void setField(Object target, String name, Object value) throws Exception {
        java.lang.reflect.Field field = target.getClass().getSuperclass().getDeclaredField(name);
        field.setAccessible(true);
        field.set(target, value);
    }
}
