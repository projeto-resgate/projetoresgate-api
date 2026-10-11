CREATE TABLE IF NOT EXISTS natural_person_role (
    id UUID PRIMARY KEY,
    natural_person_id UUID NOT NULL REFERENCES natural_person(id),
    role VARCHAR(30) NOT NULL,
    date_created TIMESTAMP,
    date_updated TIMESTAMP,
    deleted_at TIMESTAMP
);

CREATE UNIQUE INDEX IF NOT EXISTS uq_natural_person_role_natural_person_role
    ON natural_person_role(natural_person_id, role) WHERE deleted_at IS NULL;

CREATE INDEX IF NOT EXISTS idx_natural_person_role_natural_person_id ON natural_person_role(natural_person_id);
CREATE INDEX IF NOT EXISTS idx_natural_person_role_deleted_at ON natural_person_role(deleted_at);