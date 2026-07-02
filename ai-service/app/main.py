from fastapi import FastAPI

from app.config import settings
from app.routes import classify, evaluate, extract, generate, health


def create_app() -> FastAPI:
    app = FastAPI(
        title="DeployPilot AI Service",
        description="AI workflow service foundation for DeployPilot.",
        version="0.1.0",
    )

    app.state.settings = settings
    app.include_router(health.router)
    app.include_router(classify.router)
    app.include_router(extract.router)
    app.include_router(generate.router)
    app.include_router(evaluate.router)
    return app


app = create_app()
