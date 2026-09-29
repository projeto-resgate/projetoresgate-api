ALTER TABLE address
    ADD COLUMN IF NOT EXISTS street_type VARCHAR(50),
    ADD COLUMN IF NOT EXISTS street_name VARCHAR(255),
    ADD COLUMN IF NOT EXISTS reference_point VARCHAR(255);

CREATE TABLE IF NOT EXISTS family_group (
    id UUID PRIMARY KEY,
    friendly_id VARCHAR(20) NOT NULL,
    name VARCHAR(255) NOT NULL,
    household_income NUMERIC(15, 2),
    per_capita_income NUMERIC(15, 2),
    education_expense NUMERIC(15, 2),
    health_expense NUMERIC(15, 2),
    housing_expense NUMERIC(15, 2),
    number_of_residents INTEGER,
    address_id UUID REFERENCES address(id),
    date_created TIMESTAMP,
    date_updated TIMESTAMP,
    deleted_at TIMESTAMP
);

CREATE SEQUENCE IF NOT EXISTS family_group_friendly_id_seq START WITH 1;

CREATE TABLE IF NOT EXISTS family_group_natural_person (
    family_group_id UUID NOT NULL REFERENCES family_group(id),
    natural_person_id UUID NOT NULL REFERENCES natural_person(id),
    PRIMARY KEY (family_group_id, natural_person_id)
);

CREATE UNIQUE INDEX IF NOT EXISTS idx_family_group_friendly_id ON family_group(friendly_id);
CREATE INDEX IF NOT EXISTS idx_family_group_name ON family_group(name);
CREATE INDEX IF NOT EXISTS idx_family_group_household_income ON family_group(household_income);
CREATE INDEX IF NOT EXISTS idx_family_group_per_capita_income ON family_group(per_capita_income);
CREATE INDEX IF NOT EXISTS idx_family_group_number_of_residents ON family_group(number_of_residents);
CREATE INDEX IF NOT EXISTS idx_family_group_address_id ON family_group(address_id);
CREATE INDEX IF NOT EXISTS idx_family_group_deleted_at ON family_group(deleted_at);
CREATE INDEX IF NOT EXISTS idx_family_group_natural_person_natural_person_id ON family_group_natural_person(natural_person_id);
