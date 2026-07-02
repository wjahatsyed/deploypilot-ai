# DeployPilot AI Service

FastAPI foundation for enterprise AI workflow support in DeployPilot.

## Requirements

- Python 3.12
- pip

## Setup

```bash
python -m venv .venv
.venv\Scripts\activate
pip install -r requirements.txt
```

## Configuration

The service reads configuration from environment variables or a `.env` file:

- `LLM_ENABLED`: Set to `true` to enable LLM calls. Defaults to `false` (mock mode).
- `LLM_PROVIDER`: LLM provider to use (e.g., `openai`). Defaults to `openai`.
- `OPENAI_API_KEY`: Required if `LLM_ENABLED` is `true`.
- `MODEL_NAME`: LLM model name. Defaults to `gpt-4-turbo-preview`.
- `REQUEST_TIMEOUT_SECONDS`: Timeout for LLM requests in seconds. Defaults to `30`.

### Local Mock Mode

If `LLM_ENABLED=false` or `OPENAI_API_KEY` is missing, the service uses `MockLlmClient` which provides deterministic mock responses for all endpoints.

## API Endpoints

### 1. Classification (`/classify`)

Classifies workflow input into categories like `sla_breach`, `refund_request`, etc.

**Request:**
```json
{
  "input_content": "Customer is complaining about a delayed refund.",
  "workflow_id": "wf-123"
}
```

**Response:**
```json
{
  "intent": "refund_request",
  "confidence": 0.95,
  "reasoningSummary": "Input explicitly mentions refund.",
  "model_name": "gpt-4-turbo-preview"
}
```

### 2. Extraction (`/extract`)

Extracts structured JSON fields from text.

**Request:**
```json
{
  "input_content": "Our office in Berlin, Germany is facing a major outage."
}
```

**Response:**
```json
{
  "city": "Berlin",
  "country": "Germany",
  "issueType": "Outage",
  "requestedAction": null,
  "customerImpact": "Major",
  "prioritySignals": ["Critical"],
  "model_name": "gpt-4-turbo-preview"
}
```

### 3. Action Generation (`/generate`)

Produces recommended actions and next steps.

**Request:**
```json
{
  "prompt": "User wants to deploy to production."
}
```

**Response:**
```json
{
  "recommendedAction": "Perform a canary deployment.",
  "nextSteps": ["Verify health", "Full rollout"],
  "riskLevel": "medium",
  "requiresHumanApproval": true,
  "model_name": "gpt-4-turbo-preview"
}
```

### 4. Evaluation (`/evaluate`)

Compares actual AI output with expected output.

**Request:**
```json
{
  "actual_output": "Deploy to prod.",
  "expected_output": "Deployment to production environment."
}
```

**Response:**
```json
{
  "score": 0.85,
  "passed": true,
  "failureReasons": [],
  "metricBreakdown": {
    "accuracy": 0.9,
    "relevance": 0.8
  },
  "model_name": "gpt-4-turbo-preview"
}
```

## Run

```bash
uvicorn app.main:app --reload
```

Health check:

```bash
curl http://localhost:8000/health
```

OpenAPI docs are available at:

```text
http://localhost:8000/docs
```

## Test

```bash
pytest
```

## Docker

```bash
docker build -t deploypilot-ai-service .
docker run --rm -p 8000:8000 deploypilot-ai-service
```
