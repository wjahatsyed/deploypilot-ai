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

The service reads configuration from environment variables:

- `OPENAI_API_KEY`: optional placeholder for future OpenAI integration
- `MODEL_NAME`: model identifier used by mock services, defaults to `gpt-4.1-mini`
- `REQUEST_TIMEOUT`: outbound request timeout in seconds, defaults to `30`

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
