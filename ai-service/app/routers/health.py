"""Health check endpoint."""

from fastapi import APIRouter
from app.services.model_registry import ModelRegistry

router = APIRouter()


@router.get("/health")
async def health():
    return {
        "status": "UP",
        "service": "ai-service",
        "models": {
            "risk":      "loaded" if ModelRegistry.risk_model._ready      else "loading",
            "sentiment": "loaded" if ModelRegistry.sentiment_model._ready  else "loading",
            "anomaly":   "loaded" if ModelRegistry.anomaly_detector._ready else "loading",
        },
    }
