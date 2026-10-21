ALTER TABLE school_grade ADD COLUMN IF NOT EXISTS abbreviation VARCHAR(50);

CREATE INDEX IF NOT EXISTS idx_class_group_title_trgm ON class_group USING GIN (title gin_trgm_ops);

CREATE INDEX IF NOT EXISTS idx_class_school_grade_school_grade_id ON class_school_grade(school_grade_id);
