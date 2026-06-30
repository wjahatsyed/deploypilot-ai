from fastapi import APIRouter

from app.schemas.extract import ExtractRequest, ExtractResponse
from app.services.extraction_service import ExtractionService

router = APIRouter(tags=["extraction"])
extraction_service = ExtractionService()


@router.post(
    "/extract",
    response_model=ExtractResponse,
    summary="Extract structured fields",
    description="Returns mock structured field extraction for workflow input. OpenAI integration is not enabled yet.",
)
def extract(request: ExtractRequest) -> ExtractResponse:
    return extraction_service.extract(request)
