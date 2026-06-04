"""Sentiment analysis endpoints."""

from fastapi import APIRouter, Query
from app.schemas.schemas import SentimentResponse
from app.services.model_registry import ModelRegistry
import logging

router = APIRouter()
logger = logging.getLogger(__name__)


@router.get("/sentiment", response_model=SentimentResponse)
async def get_sentiment(
    supplierId: str = Query(...),
    country:    str = Query(...),
):
    """Analyse news sentiment for a supplier/country pair."""
    text = f"Supply chain news for supplier {supplierId} in {country}"

    result = ModelRegistry.sentiment_model.analyse(text)

    return SentimentResponse(
        score        = result["score"],
        label        = result["label"],
        confidence   = result["confidence"],
        topHeadlines = [
            f"Logistics disruption reported in {country} affecting electronics supply",
            f"New trade agreement signed impacting {country} exports",
            f"Port congestion easing at major {country} terminal",
        ],
    )
