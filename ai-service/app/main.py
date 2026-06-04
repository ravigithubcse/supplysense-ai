"""
SupplySense AI Service
FastAPI application serving ML predictions for supply chain risk.
"""

from fastapi import FastAPI, Request
from fastapi.middleware.cors import CORSMiddleware
from fastapi.middleware.gzip import GZipMiddleware
from contextlib import asynccontextmanager
import logging
import time

from app.routers import predict, sentiment, anomaly, health, metrics
from app.core.config import settings

# ─── Logging ────────────────────────────────────────────────────────────────

logging.basicConfig(
    level=logging.DEBUG if settings.DEBUG else logging.INFO,
    format="%(asctime)s %(levelname)s [%(name)s] %(message)s",
)
logger = logging.getLogger("supplysense.ai")


# ─── Lifespan ────────────────────────────────────────────────────────────────

@asynccontextmanager
async def lifespan(app: FastAPI):
    logger.info("SupplySense AI Service starting up…")
    # Load ML models into memory at startup
    from app.services.model_registry import ModelRegistry
    await ModelRegistry.load_all()
    logger.info("All ML models loaded successfully")
    yield
    logger.info("SupplySense AI Service shutting down…")


# ─── App Factory ──────────────────────────────────────────────────────────────

def create_app() -> FastAPI:
    app = FastAPI(
        title="SupplySense AI Service",
        description="Predictive ML service for supply chain risk intelligence",
        version="1.0.0",
        docs_url="/docs",
        redoc_url="/redoc",
        openapi_url="/openapi.json",
        lifespan=lifespan,
    )

    # ── Middleware ──────────────────────────────────────────
    app.add_middleware(GZipMiddleware, minimum_size=1000)
    app.add_middleware(
        CORSMiddleware,
        allow_origins=["*"],
        allow_credentials=True,
        allow_methods=["*"],
        allow_headers=["*"],
    )

    @app.middleware("http")
    async def add_process_time_header(request: Request, call_next):
        start = time.perf_counter()
        response = await call_next(request)
        elapsed = (time.perf_counter() - start) * 1000
        response.headers["X-Process-Time-Ms"] = f"{elapsed:.2f}"
        return response

    # ── Routers ─────────────────────────────────────────────
    app.include_router(health.router,   prefix="",             tags=["Health"])
    app.include_router(metrics.router,  prefix="",             tags=["Metrics"])
    app.include_router(predict.router,  prefix="/api/v1",      tags=["Predictions"])
    app.include_router(sentiment.router,prefix="/api/v1",      tags=["Sentiment"])
    app.include_router(anomaly.router,  prefix="/api/v1",      tags=["Anomaly Detection"])

    return app


app = create_app()
