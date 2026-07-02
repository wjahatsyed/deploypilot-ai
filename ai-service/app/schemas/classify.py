from pydantic import BaseModel, ConfigDict, Field


class ClassifyRequest(BaseModel):
    model_config = ConfigDict(json_schema_extra={
        "example": {
            "workflow_id": "workflow-123",
            "input_content": "Customer is asking to deploy release 1.2.3 to production.",
            "input_source": "email",
        }
    })

    input_content: str = Field(..., min_length=1, description="Raw workflow input to classify.")
    input_source: str | None = Field(default=None, description="Optional source channel for the input.")
    workflow_id: str | None = Field(default=None, description="Optional workflow identifier from the backend.")


class ClassifyResponse(BaseModel):
    model_config = ConfigDict(json_schema_extra={
        "example": {
            "intent": "deployment_request",
            "confidence": 0.92,
            "reasoningSummary": "Input contains production deployment keywords.",
            "model_name": "mock-classifier",
        }
    })

    intent: str
    confidence: float = Field(..., ge=0, le=1)
    reasoningSummary: str | None = None
    model_name: str
