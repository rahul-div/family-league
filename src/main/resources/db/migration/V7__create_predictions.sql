-- Match predictions
CREATE TABLE match_predictions (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    match_id UUID NOT NULL REFERENCES matches(id),
    user_id UUID NOT NULL REFERENCES users(id),
    predicted_winner_team_id UUID REFERENCES teams(id),
    predicted_toss_winner_team_id UUID REFERENCES teams(id),
    predicted_potm_player_id UUID REFERENCES players(id),
    is_locked BOOLEAN NOT NULL DEFAULT FALSE,
    submitted_at TIMESTAMPTZ,

    created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    created_by UUID,
    updated_at TIMESTAMPTZ,
    updated_by UUID,
    is_deleted BOOLEAN NOT NULL DEFAULT FALSE,
    deleted_at TIMESTAMPTZ,
    deleted_by UUID,

    UNIQUE(match_id, user_id)
);

CREATE INDEX idx_match_predictions_user_id ON match_predictions(user_id);
CREATE INDEX idx_match_predictions_match_id ON match_predictions(match_id);

-- League predictions (team final positions)
CREATE TABLE league_predictions (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    season_id UUID NOT NULL REFERENCES seasons(id),
    user_id UUID NOT NULL REFERENCES users(id),
    team_id UUID NOT NULL REFERENCES teams(id),
    predicted_position INTEGER NOT NULL,
    is_locked BOOLEAN NOT NULL DEFAULT FALSE,
    submitted_at TIMESTAMPTZ,

    created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    created_by UUID,
    updated_at TIMESTAMPTZ,
    updated_by UUID,
    is_deleted BOOLEAN NOT NULL DEFAULT FALSE,
    deleted_at TIMESTAMPTZ,
    deleted_by UUID,

    UNIQUE(season_id, user_id, team_id),
    UNIQUE(season_id, user_id, predicted_position)
);

CREATE INDEX idx_league_predictions_user_id ON league_predictions(user_id);
CREATE INDEX idx_league_predictions_season_id ON league_predictions(season_id);
