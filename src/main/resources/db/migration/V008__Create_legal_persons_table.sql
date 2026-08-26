CREATE TABLE IF NOT EXISTS address (
    id UUID PRIMARY KEY,
    zip_code VARCHAR(20) NOT NULL,
    number VARCHAR(20),
    complement VARCHAR(100),
    neighborhood VARCHAR(100),
    city VARCHAR(100) NOT NULL,
    state VARCHAR(50) NOT NULL,
    date_created TIMESTAMP,
    date_updated TIMESTAMP,
    deleted_at TIMESTAMP
);

CREATE TABLE IF NOT EXISTS legal_person (
    id UUID PRIMARY KEY,
    cnpj VARCHAR(14),
    corporate_name VARCHAR(255) NOT NULL,
    trade_name VARCHAR(255),
    display_name VARCHAR(255),
    main_cnae_code VARCHAR(20),
    registration_status VARCHAR(30) NOT NULL,
    company_status VARCHAR(30) NOT NULL,
    address_id UUID REFERENCES address(id),
    representative_name VARCHAR(255),
    representative_cellphone VARCHAR(20),
    representative_phone VARCHAR(20),
    representative_email VARCHAR(255),
    date_created TIMESTAMP,
    date_updated TIMESTAMP,
    deleted_at TIMESTAMP
);

CREATE UNIQUE INDEX IF NOT EXISTS idx_legal_person_cnpj ON legal_person(cnpj);
