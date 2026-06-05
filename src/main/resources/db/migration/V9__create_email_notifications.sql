-- Email notifications log
CREATE TABLE email_notifications (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    to_email VARCHAR(255) NOT NULL,
    user_id UUID REFERENCES users(id),
    subject VARCHAR(500),
    body TEXT,
    event_type VARCHAR(50) CHECK (event_type IN (
        'MATCH_PREDICTION_REMINDER','MATCH_PREDICTION_LOCKED',
        'RESULT_PUBLISHED','LEADERBOARD_UPDATED',
        'SEASON_STARTED','SEASON_CLOSED',
        'BULK_NOTIFICATION','WELCOME','PENDING_RESULT_ALERT')),
    status VARCHAR(20) NOT NULL DEFAULT 'PENDING'
        CHECK (status IN ('PENDING','SENT','FAILED')),
    sent_at TIMESTAMPTZ,
    retry_count INTEGER DEFAULT 0,
    error_message TEXT,
    reference_id UUID,

    created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    created_by UUID,
    updated_at TIMESTAMPTZ,
    updated_by UUID,
    is_deleted BOOLEAN NOT NULL DEFAULT FALSE,
    deleted_at TIMESTAMPTZ,
    deleted_by UUID
);

CREATE INDEX idx_email_notifications_status ON email_notifications(status);
CREATE INDEX idx_email_notifications_user_id ON email_notifications(user_id);
CREATE INDEX idx_email_notifications_event_type ON email_notifications(event_type);
