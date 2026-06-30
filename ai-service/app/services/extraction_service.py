from app.config import settings
from app.schemas.extract import ExtractRequest, ExtractResponse


class ExtractionService:
    def extract(self, request: ExtractRequest) -> ExtractResponse:
        return ExtractResponse(
            fields={
                "detected_intent": request.detected_intent or "unknown",
                "input_length": len(request.input_content),
                "requires_human_review": True,
            },
            model_name=f"mock-extractor:{settings.model_name}",
        )
