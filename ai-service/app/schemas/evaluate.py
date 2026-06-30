from pydantic import BaseModel, ConfigDict, Field


class EvaluateRequest(BaseModel):
    model_config = ConfigDict(json_schema_extra={
        "example": {
            "input_content": "Deploy release 1.2.3 to production.",
            "generated_output": "Request approval before deployment.",
            "criteria": ["safety", "completeness"],
        }
    })

    input_content: str = Field(..., min_length=1, description="Original workflow input.")
    generated_output: str = Field(..., min_length=1, description="Generated output to evaluate.")
    criteria: list[str] = Field(default_factory=list, description="Optional evaluation criteria.")


class EvaluateResponse(BaseModel):
    model_config = ConfigDict(json_schema_extra={
        "example": {
            "status": "PASSED",
            "score": 0.88,
            "summary": "Mock evaluation passed with basic safety and completeness checks.",
            "model_name": "mock-evaluator",
        }
    })

    status: str
    score: float = Field(..., ge=0, le=1)
    summary: str
    model_name: str
