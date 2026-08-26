CREATE INDEX IF NOT EXISTS idx_legal_person_registration_status ON legal_person(registration_status);
CREATE INDEX IF NOT EXISTS idx_legal_person_company_status ON legal_person(company_status);
CREATE INDEX IF NOT EXISTS idx_legal_person_address_id ON legal_person(address_id);
CREATE INDEX IF NOT EXISTS idx_legal_person_deleted_at ON legal_person(deleted_at);

CREATE INDEX IF NOT EXISTS idx_address_zip_code ON address(zip_code);
CREATE INDEX IF NOT EXISTS idx_address_city_state ON address(city, state);
CREATE INDEX IF NOT EXISTS idx_address_deleted_at ON address(deleted_at);
