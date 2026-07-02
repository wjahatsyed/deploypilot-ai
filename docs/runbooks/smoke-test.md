# End-to-End Smoke Test

Use the smoke test after `docker compose up` to verify DeployPilot AI is healthy and can run the core customer, deployment config, workflow, approval, and evaluation-summary flow.

## Prerequisites

- Docker Compose stack is running:

```bash
docker compose up --build
```

- Backend is reachable at `http://localhost:8080`.
- AI service is reachable at `http://localhost:8000`.

The scripts can use different URLs through environment variables:

- `BACKEND_URL`
- `AI_SERVICE_URL`

## macOS/Linux/Git Bash

```bash
./scripts/e2e-smoke-test.sh
```

With custom URLs:

```bash
BACKEND_URL=http://localhost:8080 AI_SERVICE_URL=http://localhost:8000 ./scripts/e2e-smoke-test.sh
```

## Windows PowerShell

```powershell
.\scripts\e2e-smoke-test.ps1
```

With custom URLs:

```powershell
$env:BACKEND_URL = "http://localhost:8080"
$env:AI_SERVICE_URL = "http://localhost:8000"
.\scripts\e2e-smoke-test.ps1
```

## What It Checks

The smoke test performs these steps:

1. Checks backend health with `GET /api/health`.
2. Checks AI service health with `GET /health`.
3. Creates a uniquely named smoke-test customer.
4. Creates a `PROD` deployment config with LLM disabled for a deterministic demo path.
5. Creates a workflow.
6. Runs the workflow with a Berlin shipment delay refund request.
7. Approves the run when it returns `WAITING_FOR_APPROVAL`.
8. Calls `GET /api/evals/summary`.

Each step prints the endpoint being called and a short result summary. The command exits non-zero on the first failure and prints a final `PASS` when the full flow succeeds.

## Expected Output Shape

```text
[1/8] Backend health: GET http://localhost:8080/api/health
  PASS: Backend is UP

[2/8] AI service health: GET http://localhost:8000/health
  PASS: AI service is UP

...

PASS: DeployPilot AI smoke test completed.
```
