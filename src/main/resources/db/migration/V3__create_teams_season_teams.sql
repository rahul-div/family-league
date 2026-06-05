-- Teams table
CREATE TABLE teams (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    name VARCHAR(255) NOT NULL UNIQUE,
    short_name VARCHAR(10),
    logo_url VARCHAR(500),
    description TEXT,

    created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    created_by UUID,
    updated_at TIMESTAMPTZ,
    updated_by UUID,
    is_deleted BOOLEAN NOT NULL DEFAULT FALSE,
    deleted_at TIMESTAMPTZ,
    deleted_by UUID
);

CREATE INDEX idx_teams_name ON teams(name);
CREATE INDEX idx_teams_is_deleted ON teams(is_deleted);

-- Season-Team enrollment (join table)
CREATE TABLE season_teams (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    season_id UUID NOT NULL REFERENCES seasons(id),
    team_id UUID NOT NULL REFERENCES teams(id),
    seed_position INTEGER,
    current_position INTEGER,

    created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    created_by UUID,
    updated_at TIMESTAMPTZ,
    updated_by UUID,
    is_deleted BOOLEAN NOT NULL DEFAULT FALSE,
    deleted_at TIMESTAMPTZ,
    deleted_by UUID,

    UNIQUE(season_id, team_id)
);

CREATE INDEX idx_season_teams_season_id ON season_teams(season_id);
CREATE INDEX idx_season_teams_team_id ON season_teams(team_id);
