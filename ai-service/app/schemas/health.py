from pydantic import BaseModel, ConfigDict


class HealthResponse(BaseModel):
    model_config = ConfigDict(json_schema_extra={
        "example": {"service": "DeployPilot AI Service", "status": "UP"}
    })

    service: str
    status: str
