"""Prometheus metrics endpoint."""

from fastapi import APIRouter, Response
from prometheus_client import (
    Counter, Histogram, generate_latest, CONTENT_TYPE_LATEST,
    CollectorRegistry, REGISTRY,
)

router = APIRouter()

prediction_counter = Counter(
    "supplysense_predictions_total",
    "Total risk predictions served",
    ["model_type"],
)

prediction_latency = Histogram(
    "supplysense_prediction_duration_seconds",
    "Risk prediction latency",
    ["model_type"],
    buckets=[0.01, 0.05, 0.1, 0.25, 0.5, 1.0, 2.5, 5.0],
)


@router.get("/metrics")
async def metrics():
    return Response(generate_latest(REGISTRY), media_type=CONTENT_TYPE_LATEST)
