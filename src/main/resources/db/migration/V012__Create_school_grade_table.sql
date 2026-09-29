CREATE TABLE IF NOT EXISTS school_grade (
    id UUID PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    grade_order INTEGER NOT NULL,
    date_created TIMESTAMP,
    date_updated TIMESTAMP,
    deleted_at TIMESTAMP
);

-- Índice único parcial: a linha excluída continua na tabela por causa do soft delete, e um índice
-- único comum impediria recadastrar uma série com o mesmo nome depois de excluí-la.
CREATE UNIQUE INDEX IF NOT EXISTS idx_school_grade_name ON school_grade(name) WHERE deleted_at IS NULL;
CREATE INDEX IF NOT EXISTS idx_school_grade_grade_order ON school_grade(grade_order);
CREATE INDEX IF NOT EXISTS idx_school_grade_deleted_at ON school_grade(deleted_at);
