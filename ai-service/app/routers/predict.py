"""Risk prediction endpoints."""

from fastapi import APIRouter, HTTPException
from app.schemas.schemas import RiskPredictionRequest, RiskPredictionResponse, RiskLevel, RiskFactor
from app.services.model_registry import ModelRegistry
import logging

router = APIRouter()
logger = logging.getLogger(__name__)

MITIGATIONS = {
    "CRITICAL": [
        "Immediately identify and qualify alternative suppliers",
        "Activate emergency procurement protocols",
        "Increase safety stock to 90-day coverage",
        "Engage senior leadership and crisis response team",
        "Initiate force-majeure clause review with legal team",
    ],
    "HIGH": [
        "Identify backup suppliers for critical components",
        "Increase safety stock to 60-day coverage",
        "Schedule executive review within 48 hours",
        "Review and update contingency logistics partners",
    ],
    "MEDIUM": [
        "Monitor supplier performance metrics weekly",
        "Increase safety stock to 30-day coverage",
        "Request supplier business continuity plan",
    ],
    "LOW": [
        "Conduct quarterly supplier risk reviews",
        "Maintain standard safety stock levels",
    ],
    "MINIMAL": [
        "Continue standard monitoring cadence",
    ],
}


def classify_risk(score: float) -> RiskLevel:
    if score >= 80: return RiskLevel.CRITICAL
    if score >= 60: return RiskLevel.HIGH
    if score >= 40: return RiskLevel.MEDIUM
    if score >= 20: return RiskLevel.LOW
    return RiskLevel.MINIMAL


@router.post("/predict/risk", response_model=RiskPredictionResponse)
async def predict_risk(request: RiskPredictionRequest):
    """
    Predict composite supply chain risk for a supplier.
    Uses LSTM + XGBoost ensemble with Prophet seasonality correction.
    """
    try:
        result = ModelRegistry.risk_model.predict(
            country=request.country,
            industry=request.industry,
            historical_metrics=request.historicalMetrics or {},
        )

        risk_level = classify_risk(result["compositeScore"])

        factors = [
            RiskFactor(name="Geopolitical",    score=result["geopoliticalScore"], weight=0.30,
                       description="Political stability, sanctions, trade disputes"),
            RiskFactor(name="Weather/Climate",  score=result["weatherScore"],      weight=0.15,
                       description="Natural disasters, seasonal disruptions"),
            RiskFactor(name="Financial",        score=result["financialScore"],    weight=0.25,
                       description="Supplier financial health, currency risk"),
            RiskFactor(name="Logistics",        score=result["logisticsScore"],    weight=0.20,
                       description="Port congestion, shipping lane disruptions"),
            RiskFactor(name="News Sentiment",   score=result["sentimentScore"],    weight=0.10,
                       description="Media sentiment and reputational risk"),
        ]

        return RiskPredictionResponse(
            supplierId       = request.supplierId,
            compositeScore   = result["compositeScore"],
            geopoliticalScore= result["geopoliticalScore"],
            weatherScore     = result["weatherScore"],
            financialScore   = result["financialScore"],
            logisticsScore   = result["logisticsScore"],
            sentimentScore   = result["sentimentScore"],
            forecastScore7d  = result["forecastScore7d"],
            forecastScore30d = result["forecastScore30d"],
            confidence       = result["confidence"],
            riskLevel        = risk_level,
            modelVersion     = result["modelVersion"],
            factors          = factors,
            mitigations      = MITIGATIONS.get(risk_level.value, []),
        )

    except Exception as e:
        logger.error("Risk prediction failed for supplier %s: %s", request.supplierId, e)
        raise HTTPException(status_code=500, detail=f"Prediction failed: {str(e)}")
