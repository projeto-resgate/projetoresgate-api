ALTER TABLE natural_person DROP CONSTRAINT IF EXISTS natural_person_cpf_key;
ALTER TABLE natural_person DROP CONSTRAINT IF EXISTS natural_person_rg_key;
CREATE UNIQUE INDEX IF NOT EXISTS uq_natural_person_cpf ON natural_person (cpf) WHERE deleted_at IS NULL;
CREATE UNIQUE INDEX IF NOT EXISTS uq_natural_person_rg ON natural_person (rg) WHERE deleted_at IS NULL;

DROP INDEX IF EXISTS idx_legal_person_cnpj;
CREATE UNIQUE INDEX IF NOT EXISTS uq_legal_person_cnpj ON legal_person (cnpj) WHERE deleted_at IS NULL;

ALTER TABLE users DROP CONSTRAINT IF EXISTS users_email_key;
CREATE UNIQUE INDEX IF NOT EXISTS uq_users_email ON users (email) WHERE deleted_at IS NULL;
