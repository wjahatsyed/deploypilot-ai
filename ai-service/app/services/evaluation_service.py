from app.config import settings
from app.schemas.evaluate import EvaluateRequest, EvaluateResponse


class EvaluationService:
    def evaluate(self, request: EvaluateRequest) -> EvaluateResponse:
        has_output = bool(request.generated_output.strip())

        return EvaluateResponse(
            status="PASSED" if has_output else "FAILED",
            score=0.88 if has_output else 0.0,
            summary="Mock evaluation completed. AI integration is not enabled yet.",
            model_name=f"mock-evaluator:{settings.model_name}",
        )
