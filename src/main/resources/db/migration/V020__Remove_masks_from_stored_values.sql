UPDATE natural_person np
SET cpf = regexp_replace(np.cpf, '[\s.\-/()]', '', 'g')
WHERE np.cpf ~ '[\s.\-/()]'
  AND NOT EXISTS (
      SELECT 1 FROM natural_person other
      WHERE other.id <> np.id
        AND other.deleted_at IS NULL
        AND regexp_replace(other.cpf, '[\s.\-/()]', '', 'g') = regexp_replace(np.cpf, '[\s.\-/()]', '', 'g')
  );

UPDATE natural_person np
SET rg = upper(regexp_replace(np.rg, '[\s.\-/()]', '', 'g'))
WHERE np.rg ~ '[\s.\-/()a-z]'
  AND NOT EXISTS (
      SELECT 1 FROM natural_person other
      WHERE other.id <> np.id
        AND other.deleted_at IS NULL
        AND upper(regexp_replace(other.rg, '[\s.\-/()]', '', 'g')) = upper(regexp_replace(np.rg, '[\s.\-/()]', '', 'g'))
  );

UPDATE natural_person
SET phone = regexp_replace(phone, '[\s.\-/()]', '', 'g')
WHERE phone ~ '[\s.\-/()]';

UPDATE natural_person
SET cellphone = regexp_replace(cellphone, '[\s.\-/()]', '', 'g')
WHERE cellphone ~ '[\s.\-/()]';

UPDATE legal_person
SET main_cnae_code = regexp_replace(main_cnae_code, '[\s.\-/()]', '', 'g')
WHERE main_cnae_code ~ '[\s.\-/()]';

UPDATE legal_person
SET representative_phone = regexp_replace(representative_phone, '[\s.\-/()]', '', 'g')
WHERE representative_phone ~ '[\s.\-/()]';

UPDATE legal_person
SET representative_cellphone = regexp_replace(representative_cellphone, '[\s.\-/()]', '', 'g')
WHERE representative_cellphone ~ '[\s.\-/()]';

UPDATE address
SET zip_code = regexp_replace(zip_code, '[\s.\-/()]', '', 'g')
WHERE zip_code ~ '[\s.\-/()]';
