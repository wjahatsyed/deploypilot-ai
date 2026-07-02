package com.deploypilot.approval;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.deploypilot.workflowrun.WorkflowRun;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(ApprovalController.class)
class ApprovalControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ApprovalService approvalService;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void findPendingReturnsList() throws Exception {
        WorkflowRun run = new WorkflowRun();
        setField(run, "id", UUID.randomUUID());
        
        HumanApproval approval = new HumanApproval();
        setField(approval, "id", UUID.randomUUID());
        approval.setWorkflowRun(run);
        approval.setStatus(ApprovalStatus.PENDING);
        approval.setRequestedBy("system");

        when(approvalService.findPending()).thenReturn(List.of(approval));

        mockMvc.perform(get("/api/approvals/pending"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].status").value("PENDING"));
    }

    @Test
    void approveRunReturnsSuccess() throws Exception {
        UUID runId = UUID.randomUUID();
        WorkflowRun run = new WorkflowRun();
        setField(run, "id", runId);
        
        HumanApproval approval = new HumanApproval();
        setField(approval, "id", UUID.randomUUID());
        approval.setWorkflowRun(run);
        approval.setStatus(ApprovalStatus.APPROVED);
        approval.setReviewerEmail("admin@example.com");

        when(approvalService.approve(eq(runId), eq("admin@example.com"), eq("ok")))
                .thenReturn(approval);

        ApprovalRequest request = new ApprovalRequest("admin@example.com", "ok");

        mockMvc.perform(post("/api/runs/{runId}/approve", runId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("APPROVED"))
                .andExpect(jsonPath("$.reviewerEmail").value("admin@example.com"));
    }

    @Test
    void rejectRunReturnsSuccess() throws Exception {
        UUID runId = UUID.randomUUID();
        WorkflowRun run = new WorkflowRun();
        setField(run, "id", runId);
        
        HumanApproval approval = new HumanApproval();
        setField(approval, "id", UUID.randomUUID());
        approval.setWorkflowRun(run);
        approval.setStatus(ApprovalStatus.REJECTED);

        when(approvalService.reject(eq(runId), any(), any()))
                .thenReturn(approval);

        ApprovalRequest request = new ApprovalRequest("admin@example.com", "no");

        mockMvc.perform(post("/api/runs/{runId}/reject", runId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("REJECTED"));
    }

    @Test
    void findByRunIdReturnsApproval() throws Exception {
        UUID runId = UUID.randomUUID();
        WorkflowRun run = new WorkflowRun();
        setField(run, "id", runId);
        
        HumanApproval approval = new HumanApproval();
        setField(approval, "id", UUID.randomUUID());
        approval.setWorkflowRun(run);
        
        when(approvalService.findByWorkflowRunId(runId)).thenReturn(Optional.of(approval));

        mockMvc.perform(get("/api/runs/{runId}/approval", runId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.workflowRunId").value(runId.toString()));
    }

    private void setField(Object target, String name, Object value) throws Exception {
        java.lang.reflect.Field field = target.getClass().getSuperclass().getDeclaredField(name);
        field.setAccessible(true);
        field.set(target, value);
    }
}
