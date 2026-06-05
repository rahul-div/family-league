-- Players table
CREATE TABLE players (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    team_id UUID NOT NULL REFERENCES teams(id),
    name VARCHAR(255) NOT NULL,
    jersey_number INTEGER,
    player_role VARCHAR(30) CHECK (player_role IN
        ('BATSMAN','BOWLER','ALL_ROUNDER','WICKET_KEEPER')),
    is_active BOOLEAN NOT NULL DEFAULT TRUE,

    created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    created_by UUID,
    updated_at TIMESTAMPTZ,
    updated_by UUID,
    is_deleted BOOLEAN NOT NULL DEFAULT FALSE,
    deleted_at TIMESTAMPTZ,
    deleted_by UUID
);

CREATE INDEX idx_players_team_id ON players(team_id);
CREATE INDEX idx_players_is_deleted ON players(is_deleted);
