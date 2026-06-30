from typing import Any

from pydantic import BaseModel, ConfigDict, Field


class GenerateRequest(BaseModel):
    model_config = ConfigDict(json_schema_extra={
        "example": {
            "prompt": "Recommend the next action for this deployment request.",
            "context": {"intent": "deployment_request", "environment": "production"},
        }
    })

    prompt: str = Field(..., min_length=1, description="Instruction or prompt for generation.")
    context: dict[str, Any] = Field(default_factory=dict, description="Optional structured workflow context.")


class GenerateResponse(BaseModel):
    model_config = ConfigDict(json_schema_extra={
        "example": {
            "recommendedAction": "Request human approval before deployment.",
            "nextSteps": ["Verify logs", "Get approval"],
            "riskLevel": "medium",
            "requiresHumanApproval": True,
            "model_name": "mock-generator",
        }
    })

    recommendedAction: str
    nextSteps: list[str] = Field(default_factory=list)
    riskLevel: str
    requiresHumanApproval: bool
    model_name: str
