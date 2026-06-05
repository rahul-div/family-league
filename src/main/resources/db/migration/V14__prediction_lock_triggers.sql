-- =============================================
-- Database-level prediction lock enforcement
-- Defense in depth: prevents writes even if
-- application-layer checks are bypassed
-- =============================================

-- Match prediction lock trigger
CREATE OR REPLACE FUNCTION enforce_match_prediction_lock()
RETURNS TRIGGER AS $$
DECLARE
    match_lock_time TIMESTAMPTZ;
    match_status VARCHAR(20);
BEGIN
    -- Get the match lock time and status
    SELECT m.prediction_lock_time, m.status
    INTO match_lock_time, match_status
    FROM matches m
    WHERE m.id = NEW.match_id;

    -- On INSERT: block if past lock time or match not SCHEDULED
    IF TG_OP = 'INSERT' THEN
        IF match_status <> 'SCHEDULED' OR now() > match_lock_time THEN
            RAISE EXCEPTION 'PREDICTION_LOCKED: Match prediction window has closed';
        END IF;
        RETURN NEW;
    END IF;

    -- On UPDATE: allow soft-delete and audit-only updates
    IF TG_OP = 'UPDATE' THEN
        -- Allow soft-delete operations
        IF NEW.is_deleted = TRUE AND OLD.is_deleted = FALSE THEN
            RETURN NEW;
        END IF;

        -- Allow audit field updates only (no prediction field changes)
        IF OLD.predicted_winner_team_id IS NOT DISTINCT FROM NEW.predicted_winner_team_id
           AND OLD.predicted_toss_winner_team_id IS NOT DISTINCT FROM NEW.predicted_toss_winner_team_id
           AND OLD.predicted_potm_player_id IS NOT DISTINCT FROM NEW.predicted_potm_player_id THEN
            RETURN NEW;
        END IF;

        -- Block prediction changes if locked
        IF NEW.is_locked = TRUE OR now() > match_lock_time OR match_status <> 'SCHEDULED' THEN
            RAISE EXCEPTION 'PREDICTION_LOCKED: Match prediction window has closed';
        END IF;
    END IF;

    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER trg_enforce_match_prediction_lock
    BEFORE INSERT OR UPDATE ON match_predictions
    FOR EACH ROW
    EXECUTE FUNCTION enforce_match_prediction_lock();

-- League prediction lock trigger
CREATE OR REPLACE FUNCTION enforce_league_prediction_lock()
RETURNS TRIGGER AS $$
DECLARE
    season_status VARCHAR(30);
    season_lock_time TIMESTAMPTZ;
BEGIN
    -- Get the season status and lock time
    SELECT s.status, s.prediction_locked_at
    INTO season_status, season_lock_time
    FROM seasons s
    WHERE s.id = NEW.season_id;

    -- On INSERT: block if season is locked or past lock time
    IF TG_OP = 'INSERT' THEN
        IF season_status IN ('PREDICTION_LOCKED', 'IN_PROGRESS', 'COMPLETED', 'CLOSED') THEN
            RAISE EXCEPTION 'PREDICTION_LOCKED: League prediction window has closed';
        END IF;
        IF season_lock_time IS NOT NULL AND now() > season_lock_time THEN
            RAISE EXCEPTION 'PREDICTION_LOCKED: League prediction window has closed';
        END IF;
        RETURN NEW;
    END IF;

    -- On UPDATE: allow soft-delete and audit-only updates
    IF TG_OP = 'UPDATE' THEN
        -- Allow soft-delete operations
        IF NEW.is_deleted = TRUE AND OLD.is_deleted = FALSE THEN
            RETURN NEW;
        END IF;

        -- Allow audit field updates only
        IF OLD.predicted_position = NEW.predicted_position
           AND OLD.team_id IS NOT DISTINCT FROM NEW.team_id THEN
            RETURN NEW;
        END IF;

        -- Block position changes if locked
        IF NEW.is_locked = TRUE
           OR season_status IN ('PREDICTION_LOCKED', 'IN_PROGRESS', 'COMPLETED', 'CLOSED') THEN
            RAISE EXCEPTION 'PREDICTION_LOCKED: League prediction window has closed';
        END IF;
        IF season_lock_time IS NOT NULL AND now() > season_lock_time THEN
            RAISE EXCEPTION 'PREDICTION_LOCKED: League prediction window has closed';
        END IF;
    END IF;

    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER trg_enforce_league_prediction_lock
    BEFORE INSERT OR UPDATE ON league_predictions
    FOR EACH ROW
    EXECUTE FUNCTION enforce_league_prediction_lock();
