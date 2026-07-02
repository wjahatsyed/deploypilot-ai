from fastapi import APIRouter

from app.schemas.generate import GenerateRequest, GenerateResponse
from app.services.action_generation_service import ActionGenerationService

router = APIRouter(tags=["generation"])
action_generation_service = ActionGenerationService()


@router.post(
    "/generate",
    response_model=GenerateResponse,
    summary="Generate workflow output",
    description="Returns a generated response for workflow action. Supports OpenAI integration.",
)
def generate(request: GenerateRequest) -> GenerateResponse:
    return action_generation_service.generate(request)
