-- Default application configuration values
INSERT INTO app_config (key, value, description) VALUES
    ('match.lock.offset.hours', '1', 'Hours before match start time to lock match predictions'),
    ('league.lock.offset.hours', '4', 'Hours before first match to lock league predictions'),
    ('match.reminder.offset.hours', '2', 'Hours before lock time to send prediction reminder emails'),
    ('jwt.expiry.seconds', '86400', 'JWT token time-to-live in seconds (default 24 hours)'),
    ('leaderboard.recalc.async', 'true', 'Whether leaderboard recalculation runs asynchronously'),
    ('result.pending.alert.hours', '2', 'Hours after match completion to alert admin about missing results');
