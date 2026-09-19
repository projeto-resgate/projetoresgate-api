CREATE TABLE IF NOT EXISTS program (
    id UUID PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    web_site_url VARCHAR(255),
    status VARCHAR(20) NOT NULL,
    institution_id UUID REFERENCES legal_person(id),
    date_created TIMESTAMP,
    date_updated TIMESTAMP,
    deleted_at TIMESTAMP
);

CREATE TABLE IF NOT EXISTS educator_category_item (
    id UUID PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    program_id UUID NOT NULL REFERENCES program(id),
    date_created TIMESTAMP,
    date_updated TIMESTAMP,
    deleted_at TIMESTAMP
);

CREATE INDEX IF NOT EXISTS idx_program_institution_id ON program(institution_id);
CREATE INDEX IF NOT EXISTS idx_program_status ON program(status);
CREATE INDEX IF NOT EXISTS idx_program_deleted_at ON program(deleted_at);
CREATE INDEX IF NOT EXISTS idx_educator_category_item_program_id ON educator_category_item(program_id);
CREATE INDEX IF NOT EXISTS idx_educator_category_item_deleted_at ON educator_category_item(deleted_at);
