from fastapi import APIRouter

from app.schemas.generate import GenerateRequest, GenerateResponse
from app.services.llm_client import LlmClient

router = APIRouter(tags=["generation"])
llm_client = LlmClient()


@router.post(
    "/generate",
    response_model=GenerateResponse,
    summary="Generate workflow output",
    description="Returns a mock generated response. OpenAI integration is not enabled yet.",
)
def generate(request: GenerateRequest) -> GenerateResponse:
    return llm_client.generate(request)
