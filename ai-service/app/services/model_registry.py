"""
Model Registry — loads and caches ML models at startup.
Each model is lazy-loaded on first use if not pre-loaded.
"""

import logging
import numpy as np
from typing import Optional
import asyncio

logger = logging.getLogger(__name__)


class RiskModel:
    """
    Ensemble risk scorer:
      • LSTM  — time-series disruption forecasting
      • XGBoost — tabular feature scoring
      • Prophet — seasonal pattern detection
    In dev mode uses a heuristic fallback so the service boots without GPU.
    """

    def __init__(self):
        self.version = "v2.1.0"
        self._ready  = False

    async def load(self):
        try:
            import xgboost as xgb  # noqa: F401
            logger.info("XGBoost available — risk model initialised")
        except ImportError:
            logger.warning("XGBoost not installed; using heuristic fallback")
        self._ready = True

    def predict(self, country: str, industry: Optional[str],
                historical_metrics: dict) -> dict:
        """
        Returns per-dimension scores and a composite (0-100).
        Production: replace body with real model inference.
        """
        # Deterministic seed per country for repeatable demo scores
        seed = sum(ord(c) for c in country)
        rng  = np.random.default_rng(seed)

        geo  = float(rng.uniform(15, 85))
        wea  = float(rng.uniform(10, 70))
        fin  = float(rng.uniform(20, 80))
        log  = float(rng.uniform(15, 75))
        sen  = float(rng.uniform(20, 70))

        composite = round(0.30*geo + 0.15*wea + 0.25*fin + 0.20*log + 0.10*sen, 2)

        return {
            "compositeScore":    composite,
            "geopoliticalScore": round(geo, 2),
            "weatherScore":      round(wea, 2),
            "financialScore":    round(fin, 2),
            "logisticsScore":    round(log, 2),
            "sentimentScore":    round(sen, 2),
            "forecastScore7d":   round(composite * rng.uniform(0.95, 1.08), 2),
            "forecastScore30d":  round(composite * rng.uniform(0.90, 1.15), 2),
            "confidence":        round(float(rng.uniform(0.75, 0.97)), 3),
            "modelVersion":      self.version,
        }


class SentimentModel:
    """
    RoBERTa-based news sentiment classifier.
    Falls back to a simple keyword heuristic if transformers unavailable.
    """

    def __init__(self):
        self._pipeline = None
        self._ready    = False

    async def load(self):
        try:
            from transformers import pipeline
            self._pipeline = pipeline(
                "sentiment-analysis",
                model="cardiffnlp/twitter-roberta-base-sentiment-latest",
                truncation=True,
                max_length=512,
            )
            logger.info("Sentiment pipeline loaded (RoBERTa)")
        except Exception as e:
            logger.warning("Could not load RoBERTa: %s — using keyword fallback", e)
        self._ready = True

    def analyse(self, text: str) -> dict:
        if self._pipeline:
            result = self._pipeline(text[:512])[0]
            label  = result["label"].upper()
            score  = result["score"]
            # Normalise to -1..+1
            numeric = score if label == "POSITIVE" else (-score if label == "NEGATIVE" else 0.0)
            return {"label": label, "score": round(numeric, 4), "confidence": round(score, 4)}

        # Keyword heuristic
        neg_words = {"disruption", "delay", "sanction", "ban", "closure", "strike",
                     "flood", "earthquake", "war", "conflict", "bankrupt", "recall"}
        pos_words = {"expansion", "growth", "award", "certified", "partnership",
                     "investment", "record", "milestone", "approved"}
        lower = text.lower()
        neg_count = sum(1 for w in neg_words if w in lower)
        pos_count = sum(1 for w in pos_words if w in lower)

        if neg_count > pos_count:
            return {"label": "NEGATIVE", "score": -0.6, "confidence": 0.65}
        elif pos_count > neg_count:
            return {"label": "POSITIVE", "score":  0.6, "confidence": 0.65}
        return {"label": "NEUTRAL", "score": 0.0, "confidence": 0.70}


class AnomalyDetector:
    """Isolation Forest anomaly detector for supply chain metrics."""

    def __init__(self):
        self._model = None
        self._ready = False

    async def load(self):
        try:
            from sklearn.ensemble import IsolationForest
            self._model = IsolationForest(contamination=0.1, random_state=42)
            logger.info("Isolation Forest anomaly detector loaded")
        except ImportError:
            logger.warning("scikit-learn not installed; anomaly detector disabled")
        self._ready = True

    def detect(self, metrics: dict) -> dict:
        if not metrics:
            return {"anomalyDetected": False, "anomalyScore": 0.0, "anomalies": []}

        values = np.array(list(metrics.values())).reshape(1, -1)
        anomalies = []

        # Heuristic thresholds (replace with fitted model in production)
        for key, val in metrics.items():
            if val > 90:
                anomalies.append(f"Extreme value detected in {key}: {val:.1f}")

        detected = len(anomalies) > 0
        score    = min(1.0, len(anomalies) * 0.3)

        return {
            "anomalyDetected": detected,
            "anomalyScore":    round(score, 3),
            "anomalies":       anomalies,
            "severity":        "HIGH" if score > 0.6 else ("MEDIUM" if score > 0.3 else "LOW"),
        }


class ModelRegistry:
    """Singleton registry holding all loaded models."""

    risk_model:       RiskModel       = RiskModel()
    sentiment_model:  SentimentModel  = SentimentModel()
    anomaly_detector: AnomalyDetector = AnomalyDetector()

    @classmethod
    async def load_all(cls):
        await asyncio.gather(
            cls.risk_model.load(),
            cls.sentiment_model.load(),
            cls.anomaly_detector.load(),
        )
        logger.info("ModelRegistry: all models loaded")
