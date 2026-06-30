package com.deploypilot.workflowrun;

public enum RunStatus {
    QUEUED,
    PROCESSING,
    COMPLETED,
    WAITING_FOR_APPROVAL,
    FAILED,
    REJECTED,
    CANCELLED
}
