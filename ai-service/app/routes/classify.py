from fastapi import APIRouter

from app.schemas.classify import ClassifyRequest, ClassifyResponse
from app.services.classifier_service import ClassifierService

router = APIRouter(tags=["classification"])
classifier_service = ClassifierService()


@router.post(
    "/classify",
    response_model=ClassifyResponse,
    summary="Classify workflow input",
    description="Returns an intent classification for workflow input. Supports OpenAI integration.",
)
def classify(request: ClassifyRequest) -> ClassifyResponse:
    return classifier_service.classify(request)
