"""Application configuration via environment variables."""

from pydantic_settings import BaseSettings
from functools import lru_cache


class Settings(BaseSettings):
    # App
    APP_NAME: str = "SupplySense AI Service"
    DEBUG: bool = False
    ENVIRONMENT: str = "development"

    # Database
    DATABASE_URL: str = "postgresql+asyncpg://supplysense:supplysense_dev_password@localhost:5432/supplysense"

    # Redis
    REDIS_URL: str = "redis://:redis_dev_password@localhost:6379"

    # MinIO / S3
    MINIO_ENDPOINT: str = "http://localhost:9000"
    MINIO_ACCESS_KEY: str = "minio_admin"
    MINIO_SECRET_KEY: str = "minio_dev_password"
    MODEL_BUCKET: str = "supplysense-models"

    # MLflow
    MLFLOW_TRACKING_URI: str = "http://localhost:5000"

    # News API (for sentiment)
    NEWS_API_KEY: str = ""
    GDELT_API_URL: str = "https://api.gdeltproject.org/api/v2/summary/summary"

    # Model versions
    RISK_MODEL_VERSION: str = "v2.1.0"
    SENTIMENT_MODEL_NAME: str = "cardiffnlp/twitter-roberta-base-sentiment-latest"

    # Inference
    BATCH_SIZE: int = 32
    MAX_SEQUENCE_LENGTH: int = 512

    class Config:
        env_file = ".env"
        case_sensitive = True


@lru_cache()
def get_settings() -> Settings:
    return Settings()


settings = get_settings()
