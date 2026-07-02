import os
from app.config import settings
from app.schemas.classify import ClassifyRequest, ClassifyResponse
from app.services.llm_client import get_llm_client


class ClassifierService:
    def __init__(self):
        self.llm_client = get_llm_client()
        prompt_path = os.path.join(os.path.dirname(__file__), "..", "prompts", "classify_prompt.txt")
        with open(prompt_path, "r") as f:
            self.prompt_template = f.read()

    def classify(self, request: ClassifyRequest) -> ClassifyResponse:
        variables = {"input_content": request.input_content}
        result = self.llm_client.call(self.prompt_template, variables)

        # Safety fallback
        if "error" in result:
            return ClassifyResponse(
                intent="unknown",
                confidence=0.0,
                reasoningSummary=f"Fallback due to error: {result.get('error')}",
                model_name=settings.model_name if settings.llm_enabled else "mock-classifier"
            )

        return ClassifyResponse(
            intent=result.get("intent", "unknown"),
            confidence=result.get("confidence", 0.0),
            reasoningSummary=result.get("reasoningSummary"),
            model_name=settings.model_name if settings.llm_enabled else "mock-classifier"
        )
