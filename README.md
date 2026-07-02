# DeployPilot AI

[![CI](https://github.com/wjahatsyed/deploypilot-ai/actions/workflows/ci.yml/badge.svg)](https://github.com/wjahatsyed/deploypilot-ai/actions/workflows/ci.yml)

## Release and Interview Docs
- [v1.0 Release Notes](docs/release/v1.0.md)
- [OpenAI Application Summary](docs/runbooks/openai-application-summary.md)
- [Interview Walkthrough](docs/runbooks/interview-walkthrough.md)

## Prerequisites
- JDK 21
- Docker and Docker Compose
- Maven (optional, Maven wrapper included)

## Getting Started

### Using Docker Compose (Recommended)
```bash
docker compose up --build
```
See [Local Setup Guide](docs/runbooks/local-setup.md) for health URLs and sample API commands.

### End-to-End Smoke Test
After the Docker Compose stack is running, verify the core platform flow with:

```bash
./scripts/e2e-smoke-test.sh
```

On Windows PowerShell:

```powershell
.\scripts\e2e-smoke-test.ps1
```

See [Smoke Test Runbook](docs/runbooks/smoke-test.md) for details and configurable URLs.

### Manual Setup
#### Database & Infrastructure
Start PostgreSQL and Redis from the repository root:
```bash
docker compose up -d postgres redis
```

## AI Service Integration
The backend integrates with the AI service to process workflow runs.

### Flow
1. User calls `POST /api/workflows/{workflowId}/runs`.
2. Backend saves a `WorkflowRun` with `PROCESSING` status.
3. Backend calls AI service `/classify` to detect intent.
4. Backend calls AI service `/extract` to pull structured fields.
5. Backend calls AI service `/generate` to get a recommended action.
6. Backend updates `WorkflowRun` with results and sets status to `COMPLETED`.

### Local Run Instructions
1. **Start Infrastructure**:
   ```bash
   docker compose up -d postgres redis
   ```

2. **Start AI Service**:
   ```bash
   cd ai-service
   pip install -r requirements.txt
   uvicorn app.main:app --reload --port 8000
   ```

2. **Start Backend**:
   ```bash
   cd backend
   ..\mvnw.cmd spring-boot:run
   ```
   The backend expects the AI service at `http://localhost:8000` (configurable via `ai.service.base-url`).

### Example Workflow Run
**Request**:
```bash
POST /api/workflows/f47ac10b-58cc-4372-a567-0e02b2c3d479/runs
{
  "inputSource": "email",
  "inputContent": "Deploy release v1.2.0 to production"
}
```

**Response**:
```json
{
  "id": "7b7a66e4-6a0d-4a1e-b83b-3f26e3c1d1a1",
  "workflowId": "f47ac10b-58cc-4372-a567-0e02b2c3d479",
  "status": "COMPLETED",
  "detectedIntent": "deployment_request",
  "extractedFieldsJson": "{\"version\":\"v1.2.0\",\"environment\":\"production\"}",
  "recommendedAction": "Mock recommendation: request human approval before deployment."
}
```

## Recent Achievements & Core Values
This project demonstrates excellence across several key areas:
- **Ownership**: Delivered end-to-end features and strategic technical documentation (see [Additional Time Allowance PRD](docs/product/additional-time-allowance-phase-2-prd.md)).
- **Technical Excellence**: Built a robust **Fraud Detection Framework** with heuristic analysis and externalized configuration.
- **Impact**: Resolved live issues and enhanced platform security and data capabilities via **Kafka enhancements**.
- **Collaboration**: Coordinated with DevOps, Frontend, and BI teams for seamless feature integration and hotfix deployments.

See [CONTRIBUTIONS.md](CONTRIBUTIONS.md) for more details.

