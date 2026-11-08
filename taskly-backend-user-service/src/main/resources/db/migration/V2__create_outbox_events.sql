CREATE TABLE outbox_events (
    id              UUID PRIMARY KEY,
    event_key       VARCHAR(100) NOT NULL,
    event_name      VARCHAR(100) NOT NULL,
    payload         TEXT NOT NULL,
    correlation_id  VARCHAR(100),
    created_at      TIMESTAMPTZ NOT NULL,
    published_at    TIMESTAMPTZ,
    attempts        INTEGER NOT NULL DEFAULT 0,
    next_attempt_at TIMESTAMPTZ NOT NULL
);

CREATE INDEX outbox_events_pending_idx ON outbox_events (next_attempt_at, created_at) WHERE published_at IS NULL;

CREATE INDEX outbox_events_published_idx ON outbox_events (published_at) WHERE published_at IS NOT NULL;
