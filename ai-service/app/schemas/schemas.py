"""Pydantic request/response schemas for the AI service."""

from pydantic import BaseModel, Field
from typing import Optional, List, Dict, Any
from enum import Enum


class RiskLevel(str, Enum):
    CRITICAL = "CRITICAL"
    HIGH     = "HIGH"
    MEDIUM   = "MEDIUM"
    LOW      = "LOW"
    MINIMAL  = "MINIMAL"


# ── Risk Prediction ───────────────────────────────────────────────────────────

class RiskPredictionRequest(BaseModel):
    supplierId:         str
    country:            str
    industry:           Optional[str] = None
    historicalMetrics:  Optional[Dict[str, Any]] = Field(default_factory=dict)


class RiskFactor(BaseModel):
    name:        str
    score:       float
    weight:      float
    description: str


class RiskPredictionResponse(BaseModel):
    supplierId:       str
    compositeScore:   float = Field(ge=0, le=100)
    geopoliticalScore: float
    weatherScore:     float
    financialScore:   float
    logisticsScore:   float
    sentimentScore:   float
    forecastScore7d:  float
    forecastScore30d: float
    confidence:       float = Field(ge=0, le=1)
    riskLevel:        RiskLevel
    modelVersion:     str
    factors:          List[RiskFactor] = Field(default_factory=list)
    mitigations:      List[str]        = Field(default_factory=list)


# ── Sentiment ─────────────────────────────────────────────────────────────────

class SentimentRequest(BaseModel):
    text: str
    supplierId: Optional[str] = None
    country:    Optional[str] = None


class SentimentResponse(BaseModel):
    score:        float           # -1 (negative) to +1 (positive)
    label:        str             # POSITIVE / NEUTRAL / NEGATIVE
    confidence:   float
    topHeadlines: List[str] = Field(default_factory=list)


# ── Anomaly Detection ─────────────────────────────────────────────────────────

class AnomalyRequest(BaseModel):
    supplierId: str
    metrics:    Optional[Dict[str, float]] = Field(default_factory=dict)


class AnomalyResponse(BaseModel):
    anomalyDetected: bool
    anomalyScore:    float
    anomalies:       List[str] = Field(default_factory=list)
    severity:        Optional[str] = None
