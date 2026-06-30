from typing import Any

from pydantic import BaseModel, ConfigDict, Field


class ExtractRequest(BaseModel):
    model_config = ConfigDict(json_schema_extra={
        "example": {
            "input_content": "Deploy release 1.2.3 to production after approval from Alex.",
            "detected_intent": "deployment_request",
        }
    })

    input_content: str = Field(..., min_length=1, description="Raw workflow input to extract from.")
    detected_intent: str | None = Field(default=None, description="Optional prior classification intent.")


class ExtractResponse(BaseModel):
    model_config = ConfigDict(json_schema_extra={
        "example": {
            "fields": {
                "environment": "production",
                "release_version": "1.2.3",
                "approver": "Alex",
            },
            "model_name": "mock-extractor",
        }
    })

    fields: dict[str, Any]
    model_name: str
