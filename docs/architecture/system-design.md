# System Design: DeployPilot AI

## 1. High-Level Architecture
DeployPilot AI is a distributed system designed to automate deployment workflows using Generative AI. It consists of three primary components:
- **Backend (Spring Boot)**: Orchestrates workflows, manages state, and provides APIs.
- **AI Service (FastAPI)**: Interfaces with LLMs for classification, extraction, and generation.
- **Frontend (Next.js)**: Dashboard for monitoring and configuring deployment workflows.

## 2. Component Design

### 2.1. Backend Orchestrator
The backend manages the lifecycle of a `WorkflowRun`. It uses a PostgreSQL database for persistence and Redis for caching and session management.

### 2.2. Fraud Detection Framework
A dedicated module for analyzing deployment requests for anomalous patterns.
- **Heuristic Engine**: Rules-based analysis of input sources and content.
- **Risk Scoring**: Calculates a confidence score for each request.
- **Integration**: Requests with high risk scores are flagged for manual approval and blocked from automated execution.

### 2.3. Data Pipeline & Kafka
To support cross-team BI and real-time monitoring, the system leverages Kafka for event streaming.
- **Events**: `WorkflowStarted`, `WorkflowCompleted`, `FraudFlagged`.
- **Consumers**: BI Integration service, Audit Logging, and Notification service.

## 3. Data Flow
1. **Ingestion**: Request received via CLI, Webhook, or Dashboard.
2. **Validation**: Basic schema checks and Authentication.
3. **Fraud Check**: Heuristic analysis performed on the request.
4. **AI Processing**: Sequential calls to AI Service for intent classification and entity extraction.
5. **Execution/Approval**: Based on confidence thresholds and risk scores, the action is either executed or queued for human approval.

## 4. Infrastructure
- **Containerization**: All services are Dockerized.
- **Deployment**: Managed via Docker Compose (local) and Kubernetes (production).
- **Monitoring**: Integrated with Prometheus and Grafana for metrics and alerting.
