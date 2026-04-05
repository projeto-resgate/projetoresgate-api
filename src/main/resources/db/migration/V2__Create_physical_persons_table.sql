CREATE TABLE IF NOT EXISTS physical_persons (
    id UUID PRIMARY KEY,
    user_id UUID NOT NULL UNIQUE,
    cpf VARCHAR(14) NOT NULL UNIQUE,
    rg VARCHAR(20),
    birth_date DATE,
    gender VARCHAR(20),
    phone VARCHAR(20),
    cellphone VARCHAR(20),
    date_created TIMESTAMP,
    date_updated TIMESTAMP,
    deleted_at TIMESTAMP,
    CONSTRAINT fk_physical_persons_user FOREIGN KEY (user_id) REFERENCES users(id)
);

CREATE INDEX IF NOT EXISTS idx_physical_persons_cpf ON physical_persons(cpf);
CREATE INDEX IF NOT EXISTS idx_physical_persons_user_id ON physical_persons(user_id);
