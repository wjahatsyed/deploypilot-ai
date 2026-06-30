import os
from app.config import settings
from app.schemas.generate import GenerateRequest, GenerateResponse
from app.services.llm_client import get_llm_client

class ActionGenerationService:
    def __init__(self):
        self.llm_client = get_llm_client()
        prompt_path = os.path.join(os.path.dirname(__file__), "..", "prompts", "generate_action_prompt.txt")
        with open(prompt_path, "r") as f:
            self.prompt_template = f.read()

    def generate(self, request: GenerateRequest) -> GenerateResponse:
        # We use prompt from request if provided, otherwise fallback to template
        # The requirements say /generate should produce specific fields.
        prompt = self.prompt_template
        variables = {"input_content": request.prompt} # Using 'prompt' field as input content context

        result = self.llm_client.call(prompt, variables)

        # Safety fallback
        if "error" in result:
            return GenerateResponse(
                recommendedAction="Error during generation. Please review manually.",
                nextSteps=[],
                riskLevel="high",
                requiresHumanApproval=True,
                model_name=settings.model_name if settings.llm_enabled else "mock-generator"
            )

        return GenerateResponse(
            recommendedAction=result.get("recommendedAction", "No action recommended."),
            nextSteps=result.get("nextSteps", []),
            riskLevel=result.get("riskLevel", "high"),
            requiresHumanApproval=result.get("requiresHumanApproval", True),
            model_name=settings.model_name if settings.llm_enabled else "mock-generator"
        )
