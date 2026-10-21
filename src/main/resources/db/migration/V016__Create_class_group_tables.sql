CREATE TABLE IF NOT EXISTS class_group (
    id UUID PRIMARY KEY,
    title VARCHAR(255) NOT NULL,
    student_projections INTEGER NOT NULL,
    start_date TIMESTAMP NOT NULL,
    end_date TIMESTAMP NOT NULL,
    note TEXT,
    program_id UUID NOT NULL REFERENCES program(id),
    institution_id UUID NOT NULL REFERENCES legal_person(id),
    active BOOLEAN NOT NULL DEFAULT TRUE,
    date_created TIMESTAMP,
    date_updated TIMESTAMP,
    deleted_at TIMESTAMP
);

-- Vínculo entre a turma e as fases/séries escolares sugeridas. Não tem atributos próprios, então
-- é apenas a tabela associativa do @ManyToMany.
CREATE TABLE IF NOT EXISTS class_school_grade (
    class_group_id UUID NOT NULL REFERENCES class_group(id),
    school_grade_id UUID NOT NULL REFERENCES school_grade(id),
    PRIMARY KEY (class_group_id, school_grade_id)
);

CREATE TABLE IF NOT EXISTS class_schedule (
    id UUID PRIMARY KEY,
    class_group_id UUID NOT NULL REFERENCES class_group(id),
    week_day VARCHAR(20) NOT NULL,
    start_time TIME NOT NULL,
    end_time TIME NOT NULL,
    active BOOLEAN NOT NULL DEFAULT TRUE,
    date_created TIMESTAMP,
    date_updated TIMESTAMP,
    deleted_at TIMESTAMP
);

-- Colaborador: a pessoa física é obrigatória e a pessoa jurídica é opcional. É o agregado que a
-- turma vincula como educador, em vez de referenciar a pessoa física diretamente.
CREATE TABLE IF NOT EXISTS collaborator (
    id UUID PRIMARY KEY,
    is_voluntary BOOLEAN NOT NULL DEFAULT FALSE,
    natural_person_id UUID NOT NULL REFERENCES natural_person(id),
    legal_person_id UUID REFERENCES legal_person(id),
    date_created TIMESTAMP,
    date_updated TIMESTAMP,
    deleted_at TIMESTAMP
);

CREATE TABLE IF NOT EXISTS class_collaborator (
    id UUID PRIMARY KEY,
    class_group_id UUID NOT NULL REFERENCES class_group(id),
    collaborator_id UUID NOT NULL REFERENCES collaborator(id),
    educator_category_item_id UUID NOT NULL REFERENCES educator_category_item(id),
    active BOOLEAN NOT NULL DEFAULT TRUE,
    date_created TIMESTAMP,
    date_updated TIMESTAMP,
    deleted_at TIMESTAMP
);

CREATE TABLE IF NOT EXISTS class_learner (
    id UUID PRIMARY KEY,
    class_group_id UUID NOT NULL REFERENCES class_group(id),
    learner_id UUID NOT NULL REFERENCES learner(id),
    active BOOLEAN NOT NULL DEFAULT TRUE,
    date_created TIMESTAMP,
    date_updated TIMESTAMP,
    deleted_at TIMESTAMP
);

CREATE INDEX IF NOT EXISTS idx_class_group_program_id ON class_group(program_id);
CREATE INDEX IF NOT EXISTS idx_class_group_institution_id ON class_group(institution_id);
CREATE INDEX IF NOT EXISTS idx_class_group_active ON class_group(active);
CREATE INDEX IF NOT EXISTS idx_class_group_deleted_at ON class_group(deleted_at);

CREATE INDEX IF NOT EXISTS idx_class_schedule_class_group_id ON class_schedule(class_group_id);
CREATE INDEX IF NOT EXISTS idx_class_schedule_week_day ON class_schedule(week_day);
CREATE INDEX IF NOT EXISTS idx_class_schedule_deleted_at ON class_schedule(deleted_at);

CREATE INDEX IF NOT EXISTS idx_class_collaborator_class_group_id ON class_collaborator(class_group_id);
CREATE INDEX IF NOT EXISTS idx_class_collaborator_collaborator_id ON class_collaborator(collaborator_id);
CREATE INDEX IF NOT EXISTS idx_class_collaborator_category_id ON class_collaborator(educator_category_item_id);
CREATE INDEX IF NOT EXISTS idx_class_collaborator_deleted_at ON class_collaborator(deleted_at);

CREATE INDEX IF NOT EXISTS idx_collaborator_natural_person_id ON collaborator(natural_person_id);
CREATE INDEX IF NOT EXISTS idx_collaborator_legal_person_id ON collaborator(legal_person_id);
CREATE INDEX IF NOT EXISTS idx_collaborator_deleted_at ON collaborator(deleted_at);

CREATE INDEX IF NOT EXISTS idx_class_learner_class_group_id ON class_learner(class_group_id);
CREATE INDEX IF NOT EXISTS idx_class_learner_learner_id ON class_learner(learner_id);
CREATE INDEX IF NOT EXISTS idx_class_learner_deleted_at ON class_learner(deleted_at);

-- Índice único parcial: a linha excluída continua na tabela por causa do soft delete, e um índice
-- único comum impediria revincular o mesmo colaborador/educando depois de desvincular.
CREATE UNIQUE INDEX IF NOT EXISTS idx_class_collaborator_class_group_collaborator
    ON class_collaborator(class_group_id, collaborator_id) WHERE deleted_at IS NULL;
CREATE UNIQUE INDEX IF NOT EXISTS idx_class_learner_class_group_learner
    ON class_learner(class_group_id, learner_id) WHERE deleted_at IS NULL;

-- Uma pessoa física tem no máximo um cadastro ativo de colaborador.
CREATE UNIQUE INDEX IF NOT EXISTS idx_collaborator_natural_person
    ON collaborator(natural_person_id) WHERE deleted_at IS NULL;
