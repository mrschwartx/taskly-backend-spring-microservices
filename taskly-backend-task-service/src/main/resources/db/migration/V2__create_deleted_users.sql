CREATE TABLE deleted_users (
  user_id    UUID PRIMARY KEY,
  deleted_at TIMESTAMPTZ NOT NULL
);

CREATE INDEX deleted_users_deleted_at ON deleted_users (deleted_at);
