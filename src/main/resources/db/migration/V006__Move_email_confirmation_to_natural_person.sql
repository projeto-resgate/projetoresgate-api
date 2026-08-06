ALTER TABLE users DROP COLUMN IF EXISTS is_email_verified;

DROP TABLE IF EXISTS email_confirmation_tokens;

CREATE TABLE IF NOT EXISTS email_confirmation_tokens (
    id UUID PRIMARY KEY,
    token_hash VARCHAR(255) NOT NULL,
    natural_person_id UUID NOT NULL,
    expiry_date TIMESTAMP NOT NULL,
    CONSTRAINT fk_email_confirmation_tokens_natural_person FOREIGN KEY (natural_person_id) REFERENCES natural_person(id)
);
