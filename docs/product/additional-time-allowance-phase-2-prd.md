# Tech PRD: Additional Time Allowance (Phase 2)

| Status | Draft |
| :--- | :--- |
| **Author** | [Junie/Author] |
| **Date** | 2026-07-01 |
| **Tags** | #Workflow #Latency #Scaling |

## 1. Executive Summary
Phase 2 of the Additional Time Allowance initiative focuses on extending the processing window for complex AI-driven workflows. As deployments become more sophisticated, the 30-second timeout currently enforced is insufficient for certain extraction and generation tasks.

## 2. Problem Statement
Workflows involving large context windows or multiple external API calls often exceed the current timeout, leading to `TIMEOUT` failures in the backend. Users need a way to specify "time allowances" for high-latency tasks without compromising system-wide resource availability.

## 3. Goals & Objectives
- **Increase Timeout**: Allow specific workflow steps to run for up to 5 minutes.
- **Async Processing**: Transition from a synchronous request-response model to an asynchronous polling/webhook model for long-running AI tasks.
- **Resource Guardrails**: Implement per-customer quotas for "Additional Time Allowance" to prevent resource exhaustion.

## 4. Proposed Technical Solution

### 4.1. Backend Changes
- **Database Schema**: Update `WorkflowRun` to include `max_allowance_seconds`.
- **State Machine**: Introduce a `DEFERRED` status for runs awaiting long-running AI results.
- **Kafka Integration**: Publish "Time Allowance" events to a dedicated Kafka topic for processing by the AI service.

### 4.2. AI Service Changes
- **Worker Pattern**: Implement a Celery or similar worker pattern to process long-running jobs.
- **Callback Mechanism**: AI service will call a backend webhook `/api/runs/{runId}/callback` upon completion.

### 4.3. Frontend Changes
- **Status Indicator**: Show a "Processing (Extended)" status in the dashboard.
- **Configuration**: Allow users to toggle "Additional Time Allowance" in the workflow settings.

## 5. Success Metrics
- 95% reduction in `TIMEOUT` errors for complex workflows.
- Average processing time for extended workflows remains under 4 minutes.
- Customer satisfaction increase based on platform reliability.

## 6. Security & Compliance
- Ensure data persistence for long-running tasks adheres to existing encryption-at-rest policies.
- Validate that extended execution windows do not introduce new DOS vectors.
