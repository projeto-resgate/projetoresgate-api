CREATE TABLE IF NOT EXISTS natural_person (
    id UUID PRIMARY KEY,
    user_id UUID NOT NULL UNIQUE,
    cpf VARCHAR(11) UNIQUE,
    rg VARCHAR(20) UNIQUE,
    birth_date DATE,
    gender VARCHAR(20),
    phone VARCHAR(20),
    cellphone VARCHAR(20),
    date_created TIMESTAMP,
    date_updated TIMESTAMP,
    deleted_at TIMESTAMP,
    CONSTRAINT fk_natural_person_user FOREIGN KEY (user_id) REFERENCES users(id)
);

CREATE INDEX IF NOT EXISTS idx_natural_person_cpf ON natural_person(cpf);
CREATE INDEX IF NOT EXISTS idx_natural_person_user_id ON natural_person(user_id);
