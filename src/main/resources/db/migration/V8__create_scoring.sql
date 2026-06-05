-- Match score details (per-user per-match scoring breakdown)
CREATE TABLE match_score_details (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    match_id UUID NOT NULL REFERENCES matches(id),
    user_id UUID NOT NULL REFERENCES users(id),
    season_id UUID NOT NULL REFERENCES seasons(id),
    winner_correct BOOLEAN DEFAULT FALSE,
    toss_winner_correct BOOLEAN DEFAULT FALSE,
    potm_correct BOOLEAN DEFAULT FALSE,
    total_match_points INTEGER DEFAULT 0,
    calculated_at TIMESTAMPTZ,

    created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    created_by UUID,
    updated_at TIMESTAMPTZ,
    updated_by UUID,
    is_deleted BOOLEAN NOT NULL DEFAULT FALSE,
    deleted_at TIMESTAMPTZ,
    deleted_by UUID,

    UNIQUE(match_id, user_id)
);

CREATE INDEX idx_match_scores_season_user ON match_score_details(season_id, user_id);

-- Season score details (per-user per-team position prediction scoring)
CREATE TABLE season_score_details (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    season_id UUID NOT NULL REFERENCES seasons(id),
    user_id UUID NOT NULL REFERENCES users(id),
    team_id UUID NOT NULL REFERENCES teams(id),
    predicted_position INTEGER NOT NULL,
    actual_position INTEGER,
    points_earned INTEGER DEFAULT 0,
    calculated_at TIMESTAMPTZ,

    created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    created_by UUID,
    updated_at TIMESTAMPTZ,
    updated_by UUID,
    is_deleted BOOLEAN NOT NULL DEFAULT FALSE,
    deleted_at TIMESTAMPTZ,
    deleted_by UUID,

    UNIQUE(season_id, user_id, team_id)
);

CREATE INDEX idx_season_scores_season_user ON season_score_details(season_id, user_id);

-- Aggregated user season scores (leaderboard source)
CREATE TABLE user_season_scores (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id UUID NOT NULL REFERENCES users(id),
    season_id UUID NOT NULL REFERENCES seasons(id),
    match_points INTEGER DEFAULT 0,
    season_prediction_points INTEGER DEFAULT 0,
    total_points INTEGER DEFAULT 0,
    rank INTEGER,
    last_calculated_at TIMESTAMPTZ,

    created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    created_by UUID,
    updated_at TIMESTAMPTZ,
    updated_by UUID,
    is_deleted BOOLEAN NOT NULL DEFAULT FALSE,
    deleted_at TIMESTAMPTZ,
    deleted_by UUID,

    UNIQUE(user_id, season_id)
);

CREATE INDEX idx_user_season_scores_season ON user_season_scores(season_id);
