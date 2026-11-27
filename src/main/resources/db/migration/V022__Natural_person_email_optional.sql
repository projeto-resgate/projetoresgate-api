ALTER TABLE natural_person ALTER COLUMN email DROP NOT NULL;

UPDATE natural_person SET email = NULL WHERE trim(email) = '';
