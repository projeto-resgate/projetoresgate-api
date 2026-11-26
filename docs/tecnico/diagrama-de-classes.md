# Diagrama de classes (domínio)

Entidades do pacote `domain` de cada módulo. Para o desenho das **tabelas**, veja
[`modelo-de-dados.md`](modelo-de-dados.md); para a organização dos módulos,
[`mapa-de-modulos.md`](mapa-de-modulos.md).

## Como ler

| Notação                         | Significado                                                                                                                          |
|---------------------------------|--------------------------------------------------------------------------------------------------------------------------------------|
| Linha **sólida** (`-->`, `*--`) | **Associação JPA** de verdade (`@OneToMany`, `@ManyToOne`, `@ManyToMany`, `@OneToOne`)                                               |
| Linha **tracejada** (`..>`)     | **Referência por `UUID`**: a classe guarda só o id, sem `@ManyToOne`. A integridade vem da FK do banco e das validações nas services |
| `+tipo atributo`                | Atributo (privado na classe; getters omitidos)                                                                                       |
| `+metodo()`                     | Regra de negócio no domínio. `create`, `validate()` e `update()` existem em todas e foram omitidos, salvo quando carregam regra      |
| `<<enumeration>>`               | Enum                                                                                                                                 |

Todas as entidades (exceto os tokens) estendem `AuditableEntity` e usam **soft delete**
(`@SQLDelete` + `@SQLRestriction("deleted_at IS NULL")`):

```mermaid
classDiagram
    direction LR
    class AuditableEntity {
        <<abstract>>
        -LocalDateTime dateCreated
        -LocalDateTime dateUpdated
        -LocalDateTime deletedAt
    }
    AuditableEntity <|-- User
    AuditableEntity <|-- NaturalPerson
    AuditableEntity <|-- NaturalPersonRole
    AuditableEntity <|-- LegalPerson
    AuditableEntity <|-- Address
    AuditableEntity <|-- FamilyGroup
    AuditableEntity <|-- Program
    AuditableEntity <|-- EducatorCategoryItem
    AuditableEntity <|-- SchoolGrade
    AuditableEntity <|-- Learner
    AuditableEntity <|-- Responsible
    AuditableEntity <|-- LearnerStatusHistory
    AuditableEntity <|-- Collaborator
    AuditableEntity <|-- ClassGroup
    AuditableEntity <|-- ClassSchedule
    AuditableEntity <|-- ClassLearner
    AuditableEntity <|-- ClassCollaborator
```

`RefreshToken`, `PasswordResetToken` e `EmailConfirmationToken` **não** estendem `AuditableEntity` (não têm
soft delete: são apagados de verdade).

## 1. Visão geral

Só as classes e como se ligam (atributos nos diagramas seguintes).

```mermaid
classDiagram
    direction TB
    class User
    class NaturalPerson
    class NaturalPersonRole
    class LegalPerson
    class Address
    class FamilyGroup
    class Program
    class EducatorCategoryItem
    class SchoolGrade
    class Learner
    class Responsible
    class LearnerStatusHistory
    class Collaborator
    class ClassGroup
    class ClassSchedule
    class ClassLearner
    class ClassCollaborator

    FamilyGroup "N" --> "N" NaturalPerson : membros
    FamilyGroup --> "0..1" Address
    LegalPerson --> "0..1" Address
    Program "1" *-- "N" EducatorCategoryItem
    ClassGroup "N" --> "N" SchoolGrade
    Learner "1" *-- "N" Responsible
    Learner "1" *-- "N" LearnerStatusHistory
    ClassGroup "1" *-- "N" ClassSchedule
    ClassGroup "1" *-- "N" ClassLearner
    ClassGroup "1" *-- "N" ClassCollaborator

    Learner ..> NaturalPerson : naturalPersonId
    Learner ..> LegalPerson : institutionId
    Learner ..> SchoolGrade : schoolGradeId
    Responsible ..> NaturalPerson : naturalPersonId
    Responsible ..> SchoolGrade : schoolGradeId
    NaturalPersonRole ..> NaturalPerson : naturalPersonId
    Collaborator ..> NaturalPerson : naturalPersonId
    Collaborator ..> LegalPerson : legalPersonId
    Program ..> LegalPerson : institutionId
    ClassGroup ..> Program : programId
    ClassGroup ..> LegalPerson : institutionId
    ClassLearner ..> Learner : learnerId
    ClassCollaborator ..> Collaborator : collaboratorId
    ClassCollaborator ..> EducatorCategoryItem : educatorCategoryItemId
```

## 2. Identidade

```mermaid
classDiagram
    direction LR
    class User {
        -UUID id
        -String email
        -String password
        -String name
        -String nickname
        -Set~UserRole~ roles
        -long tokenVersion
        +create(email, encodedPassword, name, nickname) User
        +changePassword(newEncodedPassword)
        +invalidateTokens()
        +addRole(role)
    }
    class UserRole {
        <<enumeration>>
        ADMIN
    }
    class RefreshToken {
        -UUID id
        -String tokenHash
        -LocalDateTime expiryDate
        -LocalDateTime dateCreated
        +isExpired() boolean
    }
    class PasswordResetToken {
        -UUID id
        -String tokenHash
        -LocalDateTime expiryDate
        +isExpired() boolean
    }

    class NaturalPerson {
        -UUID id
        -String name
        -String email
        -String nickname
        -String cpf
        -String rg
        -LocalDate birthDate
        -Gender gender
        -String phone
        -String cellphone
        -UUID workplaceId
        -boolean isEmailVerified
        +confirmEmail()
    }
    class Gender {
        <<enumeration>>
        MALE
        FEMALE
        OTHER
    }
    class EmailConfirmationToken {
        -UUID id
        -String tokenHash
        -LocalDateTime expiryDate
        +isExpired() boolean
    }
    class NaturalPersonRole {
        -UUID id
        -UUID naturalPersonId
        -NaturalPersonRoleType role
    }
    class NaturalPersonRoleType {
        <<enumeration>>
        LEARNER
        RESPONSIBLE
    }
    class NaturalPersonRoleHolder {
        <<interface>>
        +naturalPersonId() UUID
        +role() NaturalPersonRoleType
    }

    class LegalPerson {
        -UUID id
        -String cnpj
        -String corporateName
        -String tradeName
        -String displayName
        -String mainCnaeCode
        -RegistrationStatus registrationStatus
        -CompanyStatus companyStatus
    }
    class Representative {
        <<embeddable>>
        -String name
        -String cellphone
        -String phone
        -String email
    }
    class RegistrationStatus {
        <<enumeration>>
        ACTIVE
        INAPT
        SUSPENDED
        DOWN
    }
    class CompanyStatus {
        <<enumeration>>
        ACTIVE
        INACTIVE
        BLOCKED
    }
    class Address {
        -UUID id
        -String streetType
        -String streetName
        -String zipCode
        -String number
        -String referencePoint
        -String complement
        -String neighborhood
        -String city
        -String state
    }
    class FamilyGroup {
        -UUID id
        -String friendlyId
        -String name
        -BigDecimal householdIncome
        -BigDecimal perCapitaIncome
        -BigDecimal educationExpense
        -BigDecimal healthExpense
        -BigDecimal housingExpense
        -Integer numberOfResidents
    }

    User --> UserRole : roles
    RefreshToken "N" --> "1" User
    PasswordResetToken "1" --> "1" User
    EmailConfirmationToken "1" --> "1" NaturalPerson
    NaturalPerson --> Gender
    NaturalPersonRole --> NaturalPersonRoleType
    NaturalPersonRole ..> NaturalPerson : naturalPersonId
    NaturalPersonRoleHolder ..> NaturalPersonRoleType
    LegalPerson --> RegistrationStatus
    LegalPerson --> CompanyStatus
    LegalPerson *-- "0..1" Representative
    LegalPerson --> "1" Address : sede
    FamilyGroup --> "0..1" Address
    FamilyGroup "N" --> "N" NaturalPerson : family_group_natural_person
```

> `User` e `NaturalPerson` **não se relacionam**: `User` é quem acessa o sistema; `NaturalPerson` é quem é
> cadastrada (aluno, responsável, educador).
> `LegalPerson` exige `Address` (obrigatório); em `FamilyGroup` o endereço é opcional.
> `FamilyGroup.friendlyId` usa o prefixo constante `FAM-` seguido do número da sequence.
> `RefreshToken` e `PasswordResetToken` são mapeados com `@OneToOne` para `User`, mas na prática cada login cria
> **um refresh token novo** (a coluna `user_id` não é única), então um usuário pode ter vários.

## 3. Educando e responsáveis

```mermaid
classDiagram
    direction LR
    class Learner {
        -UUID id
        -UUID naturalPersonId
        -UUID institutionId
        -UUID schoolGradeId
        -boolean interestArts
        -ScholarshipCondition scholarshipCondition
        -Integer academicYear
        -String motherName
        -String schoolEnrollment
        -LearnerStatus status
        +create(...) Learner
        +syncResponsibleList(incoming) List~Responsible~
        +dropOut(reason, eventDate)
        +reactivate(note, eventDate)
        +roleAssignments() List~NaturalPersonRoleHolder~
    }
    class Responsible {
        -UUID id
        -UUID naturalPersonId
        -ResponsibleType type
        -Kinship kinship
        -UUID schoolGradeId
        +updateFrom(changes)
    }
    class LearnerStatusHistory {
        -UUID id
        -LearnerStatusEventType type
        -String reason
        -LocalDate eventDate
    }
    class NaturalPersonRoleHolder {
        <<interface>>
    }
    class LearnerStatus {
        <<enumeration>>
        ACTIVE
        DROPPED_OUT
    }
    class LearnerStatusEventType {
        <<enumeration>>
        DROPOUT
        REACTIVATION
    }
    class ScholarshipCondition {
        <<enumeration>>
        NONE
        FULL
        PARTIAL
        SOCIAL_ASSISTANCE
    }
    class ResponsibleType {
        <<enumeration>>
        COMMUNICATION
        FINANCIAL
    }
    class Kinship {
        <<enumeration>>
        MOTHER
        FATHER
        STEP_MOTHER
        STEP_FATHER
        GRANDMOTHER
        GRANDFATHER
        AUNT
        UNCLE
        SISTER
        BROTHER
        LEGAL_GUARDIAN
        OTHER
    }
    class NaturalPerson
    class LegalPerson
    class SchoolGrade

    Learner "1" *-- "N" Responsible : cascade ALL, orphanRemoval
    Learner "1" *-- "N" LearnerStatusHistory : cascade ALL
    Learner --> LearnerStatus
    Learner --> ScholarshipCondition
    Responsible --> ResponsibleType
    Responsible --> Kinship
    LearnerStatusHistory --> LearnerStatusEventType
    Learner ..|> NaturalPersonRoleHolder
    Responsible ..|> NaturalPersonRoleHolder
    Learner ..> NaturalPerson : naturalPersonId
    Learner ..> LegalPerson : institutionId
    Learner ..> SchoolGrade : schoolGradeId
    Responsible ..> NaturalPerson : naturalPersonId
    Responsible ..> SchoolGrade : schoolGradeId
```

Regras que moram aqui (`Learner`): o educando não é responsável de si mesmo, não há responsável repetido,
`dropOut` exige motivo e falha se já desistente, `reactivate` falha se já ativo.
Ao implementar `NaturalPersonRoleHolder`, `Learner` e `Responsible` alimentam a tabela de papéis
(`LEARNER` / `RESPONSIBLE`) via `LearnerRoleSynchronizer`.

## 4. Programas, séries, colaboradores e turmas

```mermaid
classDiagram
    direction LR
    class Program {
        -UUID id
        -String name
        -String webSiteUrl
        -ProgramStatus status
        -UUID institutionId
    }
    class EducatorCategoryItem {
        -UUID id
        -String name
        +updateName(name)
    }
    class ProgramStatus {
        <<enumeration>>
        ACTIVE
        INACTIVE
    }
    class SchoolGrade {
        -UUID id
        -String name
        -String abbreviation
        -Integer gradeOrder
    }
    class Collaborator {
        -UUID id
        -boolean isVoluntary
        -UUID naturalPersonId
        -UUID legalPersonId
    }
    class ClassGroup {
        -UUID id
        -String title
        -UUID programId
        -UUID institutionId
        -Integer studentProjections
        -LocalDateTime startDate
        -LocalDateTime endDate
        -String note
        -boolean active
        +create(...) ClassGroup
        +refreshTitle(programName, weekDays)
    }
    class ClassSchedule {
        -UUID id
        -WeekDay weekDay
        -LocalTime startTime
        -LocalTime endTime
        -boolean active
        +activate()
        +deactivate()
    }
    class ClassLearner {
        -UUID id
        -UUID learnerId
        -boolean active
        +activate()
        +deactivate()
    }
    class ClassCollaborator {
        -UUID id
        -UUID collaboratorId
        -UUID educatorCategoryItemId
        -boolean active
    }
    class WeekDay {
        <<enumeration>>
        MONDAY
        TUESDAY
        WEDNESDAY
        THURSDAY
        FRIDAY
        SATURDAY
        SUNDAY
        -String abbreviation
        -DayOfWeek dayOfWeek
        +from(DayOfWeek) Optional~WeekDay~
    }
    class Learner
    class NaturalPerson
    class LegalPerson

    Program "1" *-- "N" EducatorCategoryItem : cascade ALL
    Program --> ProgramStatus
    ClassGroup "N" --> "N" SchoolGrade : class_school_grade
    ClassGroup "1" *-- "N" ClassSchedule
    ClassGroup "1" *-- "N" ClassLearner
    ClassGroup "1" *-- "N" ClassCollaborator
    ClassSchedule --> WeekDay

    Program ..> LegalPerson : institutionId
    ClassGroup ..> Program : programId
    ClassGroup ..> LegalPerson : institutionId
    ClassLearner ..> Learner : learnerId
    ClassCollaborator ..> Collaborator : collaboratorId
    ClassCollaborator ..> EducatorCategoryItem : educatorCategoryItemId
    Collaborator ..> NaturalPerson : naturalPersonId
    Collaborator ..> LegalPerson : legalPersonId
```

Regras que moram aqui:

- `ClassGroup`: o término é calculado (`startDate + duração em meses`; a duração **não** é guardada) e
  `refreshTitle` recompõe `Programa - Séries - Dias`.
- `ClassSchedule`: fim deve ser posterior ao início.
- `Collaborator`: `isVoluntary` e `legalPersonId` (empresa) são opcionais; a pessoa física é obrigatória.
- A categoria de um `ClassCollaborator` precisa pertencer ao **programa da turma**; essa checagem está na
  service (`findByIdAndProgramIdOrThrow`), não na entidade.
## Notas das últimas mudanças

- `ClassGroup.schoolGradeList` referencia `SchoolGradeRef`, uma entidade somente leitura (`@Immutable`) mapeada na
  mesma tabela `school_grade`, com o mesmo filtro de exclusão das demais entidades. Uma série usada por turma não
  pode ser excluída ([RN-GER-05](regras-implementadas.md#exclusão-e-cascatas)).
- `NaturalPerson.workplaceId` aponta para `LegalPerson` (local de trabalho), por id, sem associação JPA.
- `LearnerParticipationStatus` (`ACTIVE`, `NO_CLASS`, `DROPPED_OUT`) é um enum de resposta, **calculado** na
  listagem de educandos. O que é gravado no `Learner` continua sendo `LearnerStatus` (`ACTIVE`, `DROPPED_OUT`).
- `LearnerStatusHistory` (`DROPOUT` e `REACTIVATION`) é exposto por `GET /learner/{id}/status-history`.
