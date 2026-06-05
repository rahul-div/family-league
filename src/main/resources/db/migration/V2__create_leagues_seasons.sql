-- Leagues table
CREATE TABLE leagues (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    name VARCHAR(255) NOT NULL UNIQUE,
    description TEXT,
    sport_type VARCHAR(50),
    is_active BOOLEAN NOT NULL DEFAULT TRUE,

    created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    created_by UUID,
    updated_at TIMESTAMPTZ,
    updated_by UUID,
    is_deleted BOOLEAN NOT NULL DEFAULT FALSE,
    deleted_at TIMESTAMPTZ,
    deleted_by UUID
);

CREATE INDEX idx_leagues_name ON leagues(name);
CREATE INDEX idx_leagues_is_deleted ON leagues(is_deleted);

-- Seasons table
CREATE TABLE seasons (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    league_id UUID NOT NULL REFERENCES leagues(id),
    name VARCHAR(255) NOT NULL,
    season_number INTEGER,
    description TEXT,
    status VARCHAR(30) NOT NULL DEFAULT 'UPCOMING'
        CHECK (status IN ('UPCOMING','PREDICTION_OPEN','PREDICTION_LOCKED',
                          'IN_PROGRESS','COMPLETED','CLOSED')),
    league_prediction_lock_hours INTEGER DEFAULT 4,
    match_prediction_lock_hours INTEGER DEFAULT 1,
    first_match_at TIMESTAMPTZ,
    prediction_locked_at TIMESTAMPTZ,
    started_at TIMESTAMPTZ,
    completed_at TIMESTAMPTZ,
    closed_at TIMESTAMPTZ,

    created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    created_by UUID,
    updated_at TIMESTAMPTZ,
    updated_by UUID,
    is_deleted BOOLEAN NOT NULL DEFAULT FALSE,
    deleted_at TIMESTAMPTZ,
    deleted_by UUID
);

CREATE INDEX idx_seasons_league_id ON seasons(league_id);
CREATE INDEX idx_seasons_status ON seasons(status);
CREATE INDEX idx_seasons_is_deleted ON seasons(is_deleted);
