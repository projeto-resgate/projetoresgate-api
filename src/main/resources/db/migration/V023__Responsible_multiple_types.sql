CREATE TABLE IF NOT EXISTS learner_responsible_type (
    learner_responsible_id UUID NOT NULL REFERENCES learner_responsible(id),
    responsible_type VARCHAR(30) NOT NULL,
    PRIMARY KEY (learner_responsible_id, responsible_type)
);

INSERT INTO learner_responsible_type (learner_responsible_id, responsible_type)
SELECT id, responsible_type FROM learner_responsible
ON CONFLICT DO NOTHING;

ALTER TABLE learner_responsible DROP COLUMN responsible_type;
