# Modelo de dados

Diagrama entidade-relacionamento conforme as migrations `V001`–`V017`. Para convenções de SQL e como criar
migrations, veja [`../banco-de-dados.md`](../banco-de-dados.md).

> **Convenção geral:** todas as tabelas de negócio têm `date_created`, `date_updated` e `deleted_at`
> (via `AuditableEntity`) e usam `UUID` como chave. Colunas de auditoria foram omitidas dos diagramas.

## Identidade

```mermaid
erDiagram
    USERS ||--o{ USER_ROLES : "tem"
    USERS ||--o{ REFRESH_TOKENS : "possui"
    USERS ||--o| PASSWORD_RESET_TOKENS : "solicita"
    NATURAL_PERSON ||--o| EMAIL_CONFIRMATION_TOKENS : "confirma e-mail"
    NATURAL_PERSON ||--o{ NATURAL_PERSON_ROLE : "papéis derivados"
    FAMILY_GROUP }o--o{ NATURAL_PERSON : "family_group_natural_person"
    FAMILY_GROUP }o--o| ADDRESS : "mora em"
    LEGAL_PERSON }o--o| ADDRESS : "sede"

    USERS {
        uuid id PK
        string email UK
        string password "BCrypt"
        string name
        string nickname
        long token_version "invalida JWTs"
    }
    NATURAL_PERSON {
        uuid id PK
        string name
        string email
        string cpf "até 11 dígitos"
        string rg
        date birth_date
        enum gender "MALE FEMALE OTHER"
        boolean is_email_verified
    }
    LEGAL_PERSON {
        uuid id PK
        string cnpj UK "só dígitos, 14"
        string corporate_name "razão social"
        string display_name
        enum registration_status "ACTIVE INAPT SUSPENDED DOWN"
        enum company_status "ACTIVE INACTIVE BLOCKED"
        string representative_name
    }
    FAMILY_GROUP {
        uuid id PK
        string friendly_id UK "FAM-n"
        string name
        decimal household_income
        decimal per_capita_income
        int number_of_residents
    }
    NATURAL_PERSON_ROLE {
        uuid id PK
        uuid natural_person_id FK
        enum role "LEARNER RESPONSIBLE"
    }
```

Pontos a notar:

- **`users` e `natural_person` são separados** (V005). `User` é quem *acessa o sistema*; `NaturalPerson`
  é uma pessoa *cadastrada* (educando, responsável, educador). Hoje não há vínculo entre eles.
- **Tokens guardam só o hash.** `refresh_tokens`, `password_reset_tokens` e `email_confirmation_tokens`
  armazenam `token_hash`; o valor em texto puro só existe no cookie/e-mail.
- **Endereço é compartilhado** entre `legal_person` e `family_group` (relação 1–1 com cascade).
  Não existe endereço para `natural_person`.
- `family_group.friendly_id` vem da sequence `family_group_friendly_id_seq` (V011) com prefixo `FAM-`.

## Acadêmico

```mermaid
erDiagram
    PROGRAM ||--o{ EDUCATOR_CATEGORY_ITEM : "define categorias"
    PROGRAM }o--o| LEGAL_PERSON : "institution_id"
    LEARNER }o--|| NATURAL_PERSON : "é uma"
    LEARNER }o--o| LEGAL_PERSON : "escola"
    LEARNER }o--o| SCHOOL_GRADE : "série"
    LEARNER ||--o{ LEARNER_RESPONSIBLE : "responsáveis"
    LEARNER ||--o{ LEARNER_STATUS_HISTORY : "histórico"
    LEARNER_RESPONSIBLE }o--|| NATURAL_PERSON : "é uma"
    COLLABORATOR }o--|| NATURAL_PERSON : "é uma"
    COLLABORATOR }o--o| LEGAL_PERSON : "empresa"
    CLASS_GROUP }o--|| PROGRAM : ""
    CLASS_GROUP }o--|| LEGAL_PERSON : "instituição"
    CLASS_GROUP }o--o{ SCHOOL_GRADE : "class_school_grade"
    CLASS_GROUP ||--o{ CLASS_SCHEDULE : "horários"
    CLASS_GROUP ||--o{ CLASS_LEARNER : "educandos"
    CLASS_GROUP ||--o{ CLASS_COLLABORATOR : "educadores"
    CLASS_LEARNER }o--|| LEARNER : ""
    CLASS_COLLABORATOR }o--|| COLLABORATOR : ""
    CLASS_COLLABORATOR }o--|| EDUCATOR_CATEGORY_ITEM : "função"

    LEARNER {
        uuid id PK
        uuid natural_person_id UK "1 educando por pessoa"
        enum status "ACTIVE DROPPED_OUT"
        enum scholarship_condition "NONE FULL PARTIAL SOCIAL_ASSISTANCE"
        int academic_year
        boolean interest_arts
        string mother_name
        string school_enrollment
    }
    LEARNER_RESPONSIBLE {
        uuid id PK
        enum responsible_type "COMMUNICATION FINANCIAL"
        enum kinship "MOTHER FATHER ... OTHER"
    }
    LEARNER_STATUS_HISTORY {
        uuid id PK
        enum type "DROPOUT REACTIVATION"
        text reason
        date event_date
    }
    CLASS_GROUP {
        uuid id PK
        string title "gerado automaticamente"
        int student_projections
        datetime start_date
        datetime end_date "start + duração"
        boolean active
        string note
    }
    CLASS_SCHEDULE {
        uuid id PK
        enum week_day
        time start_time
        time end_time
        boolean active
    }
    SCHOOL_GRADE {
        uuid id PK
        string name UK
        string abbreviation
        int grade_order
    }
```

Pontos a notar:

- **`ClassLearner`, `ClassCollaborator`, `ClassSchedule` e `ClassGroup` têm flag `active`** além do
  soft delete. `active=false` é "pausado/encerrado" (reversível e visível); `deleted_at` é "removido".
- **Referências por UUID, não por `@ManyToOne`**, em várias entidades (`Learner.naturalPersonId`,
  `ClassGroup.programId`…). A integridade vem das FKs do banco e das validações nas services — o JPA
  não carrega o objeto relacionado sozinho.
- **Unicidade:** um educando por pessoa física, um colaborador por pessoa física, um vínculo
  educando↔turma e colaborador↔turma. Os índices únicos são **parciais** (`WHERE deleted_at IS NULL`),
  então uma linha excluída libera a combinação para ser recriada.

## Estados do educando

```mermaid
stateDiagram-v2
    [*] --> ACTIVE : cadastro
    ACTIVE --> DROPPED_OUT : dropout(motivo, data)
    DROPPED_OUT --> ACTIVE : reactivate(nota, hoje)
    ACTIVE --> [*] : soft delete
    DROPPED_OUT --> [*] : soft delete
```

Cada transição grava um `LearnerStatusHistory`. Detalhes em [`fluxo-educandos.md`](fluxo-educandos.md).

## Seed de desenvolvimento

`docker/database/init.sql` popula usuários admin, famílias, educandos, turmas etc. Roda **apenas**
no `docker compose` local (serviço `seeder`), depois das migrations. **Nunca roda em produção**, e
contém senhas de exemplo — não reaproveite essas credenciais.
