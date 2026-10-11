CREATE TABLE IF NOT EXISTS learner (
    id UUID PRIMARY KEY,
    natural_person_id UUID NOT NULL REFERENCES natural_person(id),
    institution_id UUID REFERENCES legal_person(id),
    school_grade_id UUID REFERENCES school_grade(id),
    interest_arts BOOLEAN NOT NULL DEFAULT FALSE,
    scholarship_condition VARCHAR(30),
    academic_year INTEGER,
    mother_name VARCHAR(255),
    school_enrollment VARCHAR(255),
    date_created TIMESTAMP,
    date_updated TIMESTAMP,
    deleted_at TIMESTAMP
);

CREATE TABLE IF NOT EXISTS learner_responsible (
    id UUID PRIMARY KEY,
    learner_id UUID NOT NULL REFERENCES learner(id),
    natural_person_id UUID NOT NULL REFERENCES natural_person(id),
    responsible_type VARCHAR(30) NOT NULL,
    kinship VARCHAR(30) NOT NULL,
    school_grade_id UUID REFERENCES school_grade(id),
    date_created TIMESTAMP,
    date_updated TIMESTAMP,
    deleted_at TIMESTAMP
);

CREATE UNIQUE INDEX IF NOT EXISTS idx_learner_natural_person_id ON learner(natural_person_id) WHERE deleted_at IS NULL;
CREATE UNIQUE INDEX IF NOT EXISTS idx_learner_responsible_learner_natural_person
    ON learner_responsible(learner_id, natural_person_id) WHERE deleted_at IS NULL;

CREATE INDEX IF NOT EXISTS idx_learner_institution_id ON learner(institution_id);
CREATE INDEX IF NOT EXISTS idx_learner_school_grade_id ON learner(school_grade_id);
CREATE INDEX IF NOT EXISTS idx_learner_academic_year ON learner(academic_year);
CREATE INDEX IF NOT EXISTS idx_learner_deleted_at ON learner(deleted_at);

CREATE INDEX IF NOT EXISTS idx_learner_responsible_learner_id ON learner_responsible(learner_id);
CREATE INDEX IF NOT EXISTS idx_learner_responsible_natural_person_id ON learner_responsible(natural_person_id);
CREATE INDEX IF NOT EXISTS idx_learner_responsible_school_grade_id ON learner_responsible(school_grade_id);
CREATE INDEX IF NOT EXISTS idx_learner_responsible_deleted_at ON learner_responsible(deleted_at);