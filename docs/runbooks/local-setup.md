# Local Setup Guide

## Docker Compose (Recommended)
The easiest way to run the entire DeployPilot AI stack is using Docker Compose.

```bash
docker compose up --build
```

### Health URLs
- **Backend**: [http://localhost:8080/actuator/health](http://localhost:8080/actuator/health)
- **AI Service**: [http://localhost:8000/health](http://localhost:8000/health)
- **Frontend**: [http://localhost](http://localhost)

### Sample API Commands

1. **Create Customer**
   ```bash
   curl -X POST http://localhost:8080/api/customers \
     -H "Content-Type: application/json" \
     -d '{"name": "Acme Corp", "industry": "Technology", "region": "US", "contactEmail": "contact@acme.com"}'
   ```

2. **Create Workflow** (Replace `<customer_id>` with ID from step 1)
   ```bash
   curl -X POST http://localhost:8080/api/workflows \
     -H "Content-Type: application/json" \
     -d '{"customerId": "<customer_id>", "name": "Main Pipeline", "description": "Production deployment pipeline"}'
   ```

3. **Create Deployment Config**
   ```bash
   curl -X POST http://localhost:8080/api/customers/<customer_id>/deployment-configs \
     -H "Content-Type: application/json" \
     -d '{"environment": "PRODUCTION", "modelName": "gpt-4", "llmEnabled": true, "approvalRequired": true, "confidenceThreshold": 0.8, "maxMonthlyRuns": 100}'
   ```

4. **Run Workflow**
   ```bash
   curl -X POST http://localhost:8080/api/workflows/<workflow_id>/runs \
     -H "Content-Type: application/json" \
     -d '{"inputSource": "CLI", "inputContent": "Deploying version 1.2.3"}'
   ```

5. **Approve Workflow Run**
   ```bash
   curl -X POST http://localhost:8080/api/runs/<run_id>/approve \
     -H "Content-Type: application/json" \
     -d '{}'
   ```

6. **Run Evaluation Summary**
   ```bash
   curl -X POST http://localhost:8080/api/eval-datasets/<dataset_id>/run \
     -H "Content-Type: application/json" \
     -d '{}'
   ```

## Manual Setup (Non-Docker)
If you prefer to run services manually:

### Requirements
- **Java**: JDK 21
- **Database**: PostgreSQL
- **Redis**: Redis 7
- **Python**: 3.12+ (for AI service)

### Infrastructure
You can still use Docker for just the infrastructure:
```bash
docker compose up -d postgres redis
```

### Backend Setup
1. Ensure `JAVA_HOME` is set to JDK 21.
2. Build and run:
   ```bash
   ./mvnw spring-boot:run -pl backend
   ```

### AI Service Setup
1. Navigate to `ai-service`.
2. Install dependencies:
   ```bash
   pip install -r requirements.txt
   ```
3. Run with uvicorn:
   ```bash
   uvicorn app.main:app --reload
   ```
