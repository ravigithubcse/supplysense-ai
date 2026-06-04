"""Anomaly detection endpoints."""

from fastapi import APIRouter
from app.schemas.schemas import AnomalyResponse
from app.services.model_registry import ModelRegistry
import logging

router = APIRouter()
logger = logging.getLogger(__name__)


@router.get("/anomaly/{supplier_id}", response_model=AnomalyResponse)
async def detect_anomaly(supplier_id: str):
    """
    Detect supply chain anomalies for a supplier using Isolation Forest.
    In production metrics are fetched from TimescaleDB time-series data.
    """
    # Stub metrics — production would query recent KPI data
    metrics = {
        "deliveryDelayDays": 2.5,
        "qualityRejectRate": 1.2,
        "invoiceAccuracy":   98.5,
        "leadTimeVariance":  3.1,
    }

    result = ModelRegistry.anomaly_detector.detect(metrics)

    return AnomalyResponse(
        anomalyDetected = result["anomalyDetected"],
        anomalyScore    = result["anomalyScore"],
        anomalies       = result["anomalies"],
        severity        = result.get("severity"),
    )
