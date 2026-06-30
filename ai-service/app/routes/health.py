from fastapi import APIRouter

from app.schemas.health import HealthResponse

router = APIRouter(tags=["health"])


@router.get(
    "/health",
    response_model=HealthResponse,
    summary="Check service health",
    description="Returns the current health status for the DeployPilot AI service.",
)
def health() -> HealthResponse:
    return HealthResponse(service="DeployPilot AI Service", status="UP")
