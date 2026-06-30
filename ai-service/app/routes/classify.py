from fastapi import APIRouter

from app.schemas.classify import ClassifyRequest, ClassifyResponse
from app.services.classifier_service import ClassifierService

router = APIRouter(tags=["classification"])
classifier_service = ClassifierService()


@router.post(
    "/classify",
    response_model=ClassifyResponse,
    summary="Classify workflow input",
    description="Returns a mock intent classification for workflow input. OpenAI integration is not enabled yet.",
)
def classify(request: ClassifyRequest) -> ClassifyResponse:
    return classifier_service.classify(request)
