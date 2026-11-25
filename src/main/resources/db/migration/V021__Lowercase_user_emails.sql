UPDATE users u
SET email = lower(trim(u.email))
WHERE u.email <> lower(trim(u.email))
  AND NOT EXISTS (
      SELECT 1 FROM users other
      WHERE other.id <> u.id
        AND other.deleted_at IS NULL
        AND lower(trim(other.email)) = lower(trim(u.email))
  );
