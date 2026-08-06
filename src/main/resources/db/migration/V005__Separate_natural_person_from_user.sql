ALTER TABLE natural_person ADD COLUMN name VARCHAR(255);
ALTER TABLE natural_person ADD COLUMN email VARCHAR(255);
ALTER TABLE natural_person ADD COLUMN nickname VARCHAR(255);
ALTER TABLE natural_person ADD COLUMN is_email_verified BOOLEAN NOT NULL DEFAULT FALSE;

UPDATE natural_person np
SET name = u.name,
    email = u.email,
    nickname = u.nickname,
    is_email_verified = u.is_email_verified
FROM users u
WHERE np.user_id = u.id;

ALTER TABLE natural_person ALTER COLUMN name SET NOT NULL;
ALTER TABLE natural_person ALTER COLUMN email SET NOT NULL;

DROP INDEX IF EXISTS idx_natural_person_user_id;
ALTER TABLE natural_person DROP CONSTRAINT IF EXISTS fk_natural_person_user;
ALTER TABLE natural_person DROP COLUMN user_id;

CREATE INDEX IF NOT EXISTS idx_natural_person_name ON natural_person(name);
CREATE INDEX IF NOT EXISTS idx_natural_person_nickname ON natural_person(nickname);
CREATE INDEX IF NOT EXISTS idx_natural_person_email ON natural_person(email);

CREATE INDEX IF NOT EXISTS idx_natural_person_name_trgm ON natural_person USING GIN (name gin_trgm_ops);
CREATE INDEX IF NOT EXISTS idx_natural_person_nickname_trgm ON natural_person USING GIN (nickname gin_trgm_ops);
