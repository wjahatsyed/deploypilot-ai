from fastapi import APIRouter

from app.schemas.evaluate import EvaluateRequest, EvaluateResponse
from app.services.evaluation_service import EvaluationService

router = APIRouter(tags=["evaluation"])
evaluation_service = EvaluationService()


@router.post(
    "/evaluate",
    response_model=EvaluateResponse,
    summary="Evaluate workflow output",
    description="Returns a mock evaluation for a workflow output. OpenAI integration is not enabled yet.",
)
def evaluate(request: EvaluateRequest) -> EvaluateResponse:
    return evaluation_service.evaluate(request)
