from app.config import settings
from app.schemas.classify import ClassifyRequest, ClassifyResponse


class ClassifierService:
    def classify(self, request: ClassifyRequest) -> ClassifyResponse:
        normalized = request.input_content.lower()
        intent = "deployment_request" if "deploy" in normalized else "general_workflow_request"

        return ClassifyResponse(
            detected_intent=intent,
            confidence=0.92,
            model_name=f"mock-classifier:{settings.model_name}",
        )
