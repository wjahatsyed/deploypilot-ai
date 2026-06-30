from functools import lru_cache

from pydantic import Field
from pydantic_settings import BaseSettings, SettingsConfigDict


class Settings(BaseSettings):
    openai_api_key: str | None = Field(default=None, validation_alias="OPENAI_API_KEY")
    model_name: str = Field(default="gpt-4.1-mini", validation_alias="MODEL_NAME")
    request_timeout: float = Field(default=30.0, validation_alias="REQUEST_TIMEOUT", gt=0)

    model_config = SettingsConfigDict(env_file=".env", env_file_encoding="utf-8", extra="ignore")


@lru_cache
def get_settings() -> Settings:
    return Settings()


settings = get_settings()
