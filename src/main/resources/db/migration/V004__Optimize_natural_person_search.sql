CREATE EXTENSION IF NOT EXISTS pg_trgm;

CREATE INDEX IF NOT EXISTS idx_users_name_trgm ON users USING GIN (name gin_trgm_ops);
CREATE INDEX IF NOT EXISTS idx_users_nickname_trgm ON users USING GIN (nickname gin_trgm_ops);
CREATE INDEX IF NOT EXISTS idx_natural_person_cpf_trgm ON natural_person USING GIN (cpf gin_trgm_ops);
CREATE INDEX IF NOT EXISTS idx_natural_person_rg_trgm ON natural_person USING GIN (rg gin_trgm_ops);

CREATE INDEX IF NOT EXISTS idx_natural_person_cellphone ON natural_person(cellphone);
CREATE INDEX IF NOT EXISTS idx_natural_person_gender ON natural_person(gender);
