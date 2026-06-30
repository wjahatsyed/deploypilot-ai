import json
import logging
from abc import ABC, abstractmethod
from typing import Any, Dict

from openai import OpenAI
from app.config import settings

logger = logging.getLogger(__name__)

class LlmClient(ABC):
    @abstractmethod
    def call(self, prompt: str, variables: Dict[str, Any]) -> Dict[str, Any]:
        pass

class OpenAiLlmClient(LlmClient):
    def __init__(self):
        self.client = OpenAI(api_key=settings.openai_api_key)
        self.model = settings.model_name
        self.timeout = settings.request_timeout_seconds

    def call(self, prompt: str, variables: Dict[str, Any]) -> Dict[str, Any]:
        rendered_prompt = prompt
        for key, value in variables.items():
            rendered_prompt = rendered_prompt.replace(f"{{{{{key}}}}}", str(value))

        try:
            response = self.client.chat.completions.create(
                model=self.model,
                messages=[{"role": "user", "content": rendered_prompt}],
                response_format={"type": "json_object"},
                timeout=self.timeout
            )
            content = response.choices[0].message.content
            return json.loads(content)
        except Exception as e:
            logger.error(f"OpenAI API call failed: {e}")
            return {"error": str(e), "fallback": True}

class MockLlmClient(LlmClient):
    def call(self, prompt: str, variables: Dict[str, Any]) -> Dict[str, Any]:
        # Simple mock logic based on prompt type (checking keywords in prompt)
        prompt_lower = prompt.lower()
        if "classify" in prompt_lower:
            return {
                "intent": "operational_escalation",
                "confidence": 0.85,
                "reasoningSummary": "Mocked response for classification"
            }
        elif "extract" in prompt_lower:
            return {
                "city": "San Francisco",
                "country": "USA",
                "issueType": "Operational",
                "requestedAction": "Review",
                "customerImpact": "Medium",
                "prioritySignals": ["Internal"]
            }
        elif "generate" in prompt_lower:
            return {
                "recommendedAction": "Escalate to Tier 2 support",
                "nextSteps": ["Notify manager", "Update ticket"],
                "riskLevel": "low",
                "requiresHumanApproval": False
            }
        elif "evaluate" in prompt_lower:
            return {
                "score": 0.95,
                "passed": True,
                "failureReasons": [],
                "metricBreakdown": {"accuracy": 0.95, "relevance": 0.95}
            }
        return {"error": "Unknown prompt type", "fallback": True}

def get_llm_client() -> LlmClient:
    if settings.llm_enabled and settings.openai_api_key:
        return OpenAiLlmClient()
    return MockLlmClient()
