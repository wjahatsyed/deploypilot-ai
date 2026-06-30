from pydantic import BaseModel, ConfigDict, Field


class EvaluateRequest(BaseModel):
    model_config = ConfigDict(json_schema_extra={
        "example": {
            "actual_output": "Request approval before deployment.",
            "expected_output": "Recommended action is to request approval from the manager.",
        }
    })

    actual_output: str = Field(..., min_length=1)
    expected_output: str = Field(..., min_length=1)


class EvaluateResponse(BaseModel):
    model_config = ConfigDict(json_schema_extra={
        "example": {
            "score": 0.88,
            "passed": True,
            "failureReasons": [],
            "metricBreakdown": {"accuracy": 0.9, "relevance": 0.85},
            "model_name": "mock-evaluator",
        }
    })

    score: float = Field(..., ge=0, le=1)
    passed: bool
    failureReasons: list[str] = Field(default_factory=list)
    metricBreakdown: dict[str, float] = Field(default_factory=dict)
    model_name: str
