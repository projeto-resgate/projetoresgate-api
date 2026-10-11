ALTER TABLE learner ADD COLUMN IF NOT EXISTS status VARCHAR(30) NOT NULL DEFAULT 'ACTIVE';

CREATE INDEX IF NOT EXISTS idx_learner_status ON learner(status);

CREATE TABLE IF NOT EXISTS learner_status_history (
    id UUID PRIMARY KEY,
    learner_id UUID NOT NULL REFERENCES learner(id),
    type VARCHAR(30) NOT NULL,
    reason TEXT,
    event_date DATE NOT NULL,
    date_created TIMESTAMP,
    date_updated TIMESTAMP,
    deleted_at TIMESTAMP
);

CREATE INDEX IF NOT EXISTS idx_learner_status_history_learner_id ON learner_status_history(learner_id);
CREATE INDEX IF NOT EXISTS idx_learner_status_history_deleted_at ON learner_status_history(deleted_at);
