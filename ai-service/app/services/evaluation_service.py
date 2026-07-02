import os
from app.config import settings
from app.schemas.evaluate import EvaluateRequest, EvaluateResponse
from app.services.llm_client import get_llm_client


class EvaluationService:
    def __init__(self):
        self.llm_client = get_llm_client()
        prompt_path = os.path.join(os.path.dirname(__file__), "..", "prompts", "evaluate_prompt.txt")
        with open(prompt_path, "r") as f:
            self.prompt_template = f.read()

    def evaluate(self, request: EvaluateRequest) -> EvaluateResponse:
        variables = {
            "actual_output": request.actual_output,
            "expected_output": request.expected_output
        }
        result = self.llm_client.call(self.prompt_template, variables)

        # Safety fallback
        if "error" in result:
            return EvaluateResponse(
                score=0.0,
                passed=False,
                failureReasons=[f"Error during evaluation: {result.get('error')}"],
                metricBreakdown={},
                model_name=settings.model_name if settings.llm_enabled else "mock-evaluator"
            )

        return EvaluateResponse(
            score=result.get("score", 0.0),
            passed=result.get("passed", False),
            failureReasons=result.get("failureReasons", []),
            metricBreakdown=result.get("metricBreakdown", {}),
            model_name=settings.model_name if settings.llm_enabled else "mock-evaluator"
        )
