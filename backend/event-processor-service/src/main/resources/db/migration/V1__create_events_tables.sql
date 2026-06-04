-- Events Service Schema V1
CREATE TABLE IF NOT EXISTS events.domain_events (
    id           UUID        PRIMARY KEY DEFAULT uuid_generate_v4(),
    event_type   VARCHAR(100) NOT NULL,
    aggregate_id VARCHAR(200) NOT NULL,
    org_id       UUID,
    payload      TEXT,
    processed    BOOLEAN     NOT NULL DEFAULT FALSE,
    created_at   TIMESTAMPTZ NOT NULL DEFAULT NOW()
);
CREATE INDEX idx_events_type       ON events.domain_events(event_type);
CREATE INDEX idx_events_aggregate  ON events.domain_events(aggregate_id);
CREATE INDEX idx_events_org        ON events.domain_events(org_id);
