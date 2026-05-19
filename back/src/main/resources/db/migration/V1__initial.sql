CREATE TABLE IF NOT EXISTS "note" (
    "path" TEXT PRIMARY KEY,
    content TEXT NOT NULL,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    expires_at TIMESTAMPTZ,
    "version" INTEGER NOT NULL DEFAULT 1,
    password_hash TEXT
);