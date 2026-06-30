import os
from app.config import settings
from app.schemas.extract import ExtractRequest, ExtractResponse
from app.services.llm_client import get_llm_client


class ExtractionService:
    def __init__(self):
        self.llm_client = get_llm_client()
        prompt_path = os.path.join(os.path.dirname(__file__), "..", "prompts", "extract_prompt.txt")
        with open(prompt_path, "r") as f:
            self.prompt_template = f.read()

    def extract(self, request: ExtractRequest) -> ExtractResponse:
        variables = {"input_content": request.input_content}
        result = self.llm_client.call(self.prompt_template, variables)

        # Safety fallback
        if "error" in result:
            return ExtractResponse(
                city=None,
                country=None,
                issueType=None,
                requestedAction=None,
                customerImpact=None,
                prioritySignals=[],
                model_name=settings.model_name if settings.llm_enabled else "mock-extractor"
            )

        return ExtractResponse(
            city=result.get("city"),
            country=result.get("country"),
            issueType=result.get("issueType"),
            requestedAction=result.get("requestedAction"),
            customerImpact=result.get("customerImpact"),
            prioritySignals=result.get("prioritySignals", []),
            model_name=settings.model_name if settings.llm_enabled else "mock-extractor"
        )
