-- Risk Engine Schema V1

CREATE TABLE IF NOT EXISTS risk.risk_scores (
    id                  UUID         PRIMARY KEY DEFAULT uuid_generate_v4(),
    supplier_id         UUID         NOT NULL,
    org_id              UUID         NOT NULL,
    composite_score     DOUBLE PRECISION NOT NULL,
    geopolitical_score  DOUBLE PRECISION,
    weather_score       DOUBLE PRECISION,
    financial_score     DOUBLE PRECISION,
    logistics_score     DOUBLE PRECISION,
    sentiment_score     DOUBLE PRECISION,
    risk_level          VARCHAR(20)  NOT NULL,
    confidence          DOUBLE PRECISION,
    forecast_score_7d   DOUBLE PRECISION,
    forecast_score_30d  DOUBLE PRECISION,
    factors_json        TEXT,
    mitigations_json    TEXT,
    model_version       VARCHAR(50),
    calculated_at       TIMESTAMPTZ  NOT NULL DEFAULT NOW()
);

-- Convert to TimescaleDB hypertable (time-series optimisation)
SELECT create_hypertable('risk.risk_scores', 'calculated_at', if_not_exists => TRUE);

CREATE TABLE IF NOT EXISTS risk.risk_alerts (
    id                  UUID         PRIMARY KEY DEFAULT uuid_generate_v4(),
    supplier_id         UUID         NOT NULL,
    org_id              UUID         NOT NULL,
    title               VARCHAR(300) NOT NULL,
    description         TEXT         NOT NULL,
    severity            VARCHAR(20)  NOT NULL,
    type                VARCHAR(30)  NOT NULL,
    status              VARCHAR(20)  NOT NULL DEFAULT 'ACTIVE',
    risk_score_delta    DOUBLE PRECISION,
    affected_country    VARCHAR(100),
    affected_region     VARCHAR(100),
    recommendations_json TEXT,
    resolved_by         VARCHAR(200),
    resolved_at         TIMESTAMPTZ,
    resolution          TEXT,
    expires_at          TIMESTAMPTZ,
    created_at          TIMESTAMPTZ  NOT NULL DEFAULT NOW()
);

-- Indexes
CREATE INDEX idx_risk_scores_supplier  ON risk.risk_scores(supplier_id, calculated_at DESC);
CREATE INDEX idx_risk_scores_org       ON risk.risk_scores(org_id, calculated_at DESC);
CREATE INDEX idx_risk_scores_level     ON risk.risk_scores(risk_level);
CREATE INDEX idx_alerts_org_status     ON risk.risk_alerts(org_id, status);
CREATE INDEX idx_alerts_supplier       ON risk.risk_alerts(supplier_id, status);
CREATE INDEX idx_alerts_severity       ON risk.risk_alerts(severity);

-- Seed demo risk scores for the demo suppliers
INSERT INTO risk.risk_scores (supplier_id, org_id, composite_score, geopolitical_score, weather_score, financial_score, logistics_score, sentiment_score, risk_level, confidence, forecast_score_7d, forecast_score_30d, model_version)
VALUES
  ('10000000-0000-0000-0000-000000000001','00000000-0000-0000-0000-000000000001', 72.5, 78.0, 45.0, 68.0, 81.0, 70.0, 'HIGH',     0.87, 74.2, 76.8, 'v2.1.0'),
  ('10000000-0000-0000-0000-000000000002','00000000-0000-0000-0000-000000000001', 38.2, 41.0, 35.0, 32.0, 44.0, 39.0, 'LOW',      0.91, 37.5, 36.1, 'v2.1.0'),
  ('10000000-0000-0000-0000-000000000003','00000000-0000-0000-0000-000000000001', 18.1, 15.0, 22.0, 14.0, 21.0, 18.0, 'MINIMAL',  0.95, 18.9, 19.5, 'v2.1.0'),
  ('10000000-0000-0000-0000-000000000004','00000000-0000-0000-0000-000000000001', 54.7, 59.0, 42.0, 61.0, 52.0, 60.0, 'MEDIUM',   0.83, 56.1, 58.3, 'v2.1.0'),
  ('10000000-0000-0000-0000-000000000005','00000000-0000-0000-0000-000000000001', 28.3, 30.0, 25.0, 22.0, 35.0, 29.0, 'LOW',      0.93, 27.9, 26.4, 'v2.1.0'),
  ('10000000-0000-0000-0000-000000000006','00000000-0000-0000-0000-000000000001', 45.9, 50.0, 38.0, 43.0, 48.0, 50.0, 'MEDIUM',   0.85, 46.3, 47.7, 'v2.1.0'),
  ('10000000-0000-0000-0000-000000000007','00000000-0000-0000-0000-000000000001', 22.4, 20.0, 28.0, 19.0, 25.0, 20.0, 'LOW',      0.92, 23.1, 24.0, 'v2.1.0'),
  ('10000000-0000-0000-0000-000000000008','00000000-0000-0000-0000-000000000001', 63.8, 70.0, 55.0, 60.0, 68.0, 65.0, 'HIGH',     0.81, 65.2, 67.4, 'v2.1.0')
ON CONFLICT DO NOTHING;

-- Seed demo alerts
INSERT INTO risk.risk_alerts (supplier_id, org_id, title, description, severity, type, status, affected_country)
VALUES
  ('10000000-0000-0000-0000-000000000001','00000000-0000-0000-0000-000000000001',
   'High geopolitical risk – South China Sea tensions',
   'Elevated shipping disruption risk due to ongoing territorial disputes affecting key trade lanes.',
   'HIGH', 'GEOPOLITICAL', 'ACTIVE', 'China'),
  ('10000000-0000-0000-0000-000000000008','00000000-0000-0000-0000-000000000001',
   'Port congestion detected – Laem Chabang',
   'Severe container backlog at Laem Chabang port causing 8-12 day transit delays.',
   'HIGH', 'LOGISTICS', 'ACTIVE', 'Thailand'),
  ('10000000-0000-0000-0000-000000000004','00000000-0000-0000-0000-000000000001',
   'Currency volatility – Brazilian Real',
   'BRL/USD exchange rate fluctuation of +14% over 30 days impacting pricing stability.',
   'MEDIUM', 'FINANCIAL', 'ACTIVE', 'Brazil')
ON CONFLICT DO NOTHING;
