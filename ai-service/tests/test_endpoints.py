import pytest
from fastapi.testclient import TestClient
from app.main import app
from app.config import settings

client = TestClient(app)

@pytest.fixture(autouse=True)
def disable_llm():
    settings.llm_enabled = False
    yield

def test_classify_mock():
    response = client.post(
        "/classify",
        json={"input_content": "Test classification", "workflow_id": "123"}
    )
    assert response.status_code == 200
    data = response.json()
    assert "intent" in data
    assert "confidence" in data
    assert "reasoningSummary" in data
    assert data["model_name"] == "mock-classifier"

def test_extract_mock():
    response = client.post(
        "/extract",
        json={"input_content": "Extract from this text"}
    )
    assert response.status_code == 200
    data = response.json()
    assert "city" in data
    assert "country" in data
    assert "issueType" in data
    assert "prioritySignals" in data
    assert data["model_name"] == "mock-extractor"

def test_generate_mock():
    response = client.post(
        "/generate",
        json={"prompt": "Generate action for this", "context": {}}
    )
    assert response.status_code == 200
    data = response.json()
    assert "recommendedAction" in data
    assert "nextSteps" in data
    assert "riskLevel" in data
    assert "requiresHumanApproval" in data
    assert data["model_name"] == "mock-generator"

def test_evaluate_mock():
    response = client.post(
        "/evaluate",
        json={"actual_output": "output", "expected_output": "expected"}
    )
    assert response.status_code == 200
    data = response.json()
    assert "score" in data
    assert "passed" in data
    assert "metricBreakdown" in data
    assert data["model_name"] == "mock-evaluator"
