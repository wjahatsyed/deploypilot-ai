# DeployPilot AI

## Prerequisites
- JDK 21
- Docker and Docker Compose
- Maven, or IntelliJ IDEA's bundled Maven

## Getting Started
### Database
Start PostgreSQL from the repository root:
```bash
docker compose -f infra/docker-compose.yml up -d postgres
```

### Build and Run
1. Build the entire project from the root:
   ```bash
   mvn clean install
   ```

2. Run the backend application:
   ```bash
   mvn spring-boot:run -pl backend
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
1. **Start AI Service**:
   ```bash
   cd ai-service
   pip install -r requirements.txt
   uvicorn app.main:app --reload --port 8000
   ```

2. **Start Backend**:
   ```bash
   cd backend
   mvn spring-boot:run
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
