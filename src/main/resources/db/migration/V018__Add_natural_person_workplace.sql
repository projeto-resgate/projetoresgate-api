ALTER TABLE natural_person ADD COLUMN IF NOT EXISTS workplace_id UUID REFERENCES legal_person(id);

CREATE INDEX IF NOT EXISTS idx_natural_person_workplace_id ON natural_person(workplace_id);
