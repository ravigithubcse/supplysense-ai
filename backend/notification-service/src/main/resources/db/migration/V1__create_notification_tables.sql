-- Notification Service Schema V1
CREATE TABLE IF NOT EXISTS notifications.notifications (
    id                   UUID        PRIMARY KEY DEFAULT uuid_generate_v4(),
    user_id              UUID        NOT NULL,
    org_id               UUID        NOT NULL,
    title                VARCHAR(300) NOT NULL,
    message              TEXT        NOT NULL,
    channel              VARCHAR(20) NOT NULL,
    severity             VARCHAR(20) NOT NULL,
    status               VARCHAR(20) NOT NULL DEFAULT 'PENDING',
    related_entity_id    VARCHAR(200),
    related_entity_type  VARCHAR(100),
    external_id          VARCHAR(500),
    error_message        TEXT,
    sent_at              TIMESTAMPTZ,
    read_at              TIMESTAMPTZ,
    created_at           TIMESTAMPTZ NOT NULL DEFAULT NOW()
);
CREATE INDEX idx_notifications_user   ON notifications.notifications(user_id, created_at DESC);
CREATE INDEX idx_notifications_org    ON notifications.notifications(org_id);
CREATE INDEX idx_notifications_status ON notifications.notifications(status);
