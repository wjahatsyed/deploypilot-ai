from app.config import settings
from app.schemas.generate import GenerateRequest, GenerateResponse


class LlmClient:
    def generate(self, request: GenerateRequest) -> GenerateResponse:
        return GenerateResponse(
            content="Mock recommendation: review the request, validate extracted fields, and request approval.",
            model_name=f"mock-generator:{settings.model_name}",
        )
