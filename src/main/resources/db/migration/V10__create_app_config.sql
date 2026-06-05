-- Runtime-tunable application configuration (key-value store)
CREATE TABLE app_config (
    key VARCHAR(255) PRIMARY KEY,
    value VARCHAR(500) NOT NULL,
    description TEXT,
    updated_at TIMESTAMPTZ DEFAULT now()
);
