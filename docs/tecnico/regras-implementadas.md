# Regras implementadas no código

> Visão **técnica** das regras (status HTTP, classes). A versão em **linguagem de negócio**, com o que a ONG pediu
> e ainda falta, está em [`../negocio/regras-de-negocio.md`](../negocio/regras-de-negocio.md).

Todas as regras do sistema numa tabela só, com **ID estável**. Use o ID em mensagens de commit, nos nomes
de teste e em conversas com a ONG (`RN-LRN-04`). Cada regra aponta para onde está implementada.

**Como ler a coluna "HTTP":** `400` = `InternalException` (regra de negócio ou validação), `409` =
`IllegalStateException` (duplicidade ou transição de estado inválida), `404` = não encontrado.
Ver [ADR 002](../adr/002-tratamento-de-erros.md).

**Como manter:** ao criar ou mudar uma regra, atualize esta tabela no mesmo PR. IDs nunca são reaproveitados;
regra removida fica riscada (`~~RN-XXX-NN~~`).

Prefixos: `USR` usuário · `PES` pessoa física · `EMP` pessoa jurídica · `FAM` família · `END` endereço ·
`PRG` programa · `SER` série · `LRN` educando · `COL` colaborador · `TUR` turma.

## Usuário e acesso

| ID         | Regra                                                                                   | HTTP | Onde                               |
|------------|-----------------------------------------------------------------------------------------|------|------------------------------------|
| RN-USR-01  | E-mail do usuário é único                                                               | 400  | `CreateUserService`                |
| RN-USR-02  | E-mail e nome do usuário são obrigatórios                                               | 400  | `User.validate`                    |
| RN-USR-03  | Todo usuário criado nasce com o papel `ADMIN`                                           | —    | `User` (construtor)                |
| RN-USR-04  | Login com e-mail inexistente ou senha errada devolve a **mesma** resposta               | 401  | `AuthenticateUserService`          |
| RN-USR-05  | Access token dura 15 min; refresh token dura 7 dias e **rotaciona** a cada uso          | 400  | `TokenService`, `RefreshTokenServiceImpl` |
| RN-USR-06  | `logout-all` revoga todos os refresh tokens **e** invalida os access tokens (`tokenVersion`) | — | `LogoutAllService`                 |
| RN-USR-07  | Reset de senha: token de 10 min, uso único, senha nova com **≥ 6 caracteres**           | 400  | `ResetPasswordService`             |
| RN-USR-08  | `forgot-password` responde 200 sempre (não revela se o e-mail existe)                   | 200  | `RequestPasswordResetService`      |
| RN-USR-09  | Login, refresh, forgot-password e reset-password limitados a **5 req/min por IP** (camada AUTH)       | 429  | `RateLimitingFilter`               |
| RN-USR-12  | Rotas públicas de escrita (criar usuário/pessoa, confirmar e-mail) limitadas a **20 req/min por IP** (PUBLIC) | 429 | `RateLimitingFilter`            |
| RN-USR-13  | Demais rotas limitadas a **300 req/min por IP** (GENERAL); preflight `OPTIONS` não conta                | 429  | `RateLimitingFilter`               |
| RN-USR-14  | Mesmo destinatário recebe no máximo **3 e-mails/hora** (reset e confirmação); excedido, ignora em silêncio, sem erro | — | `EmailRateLimiter`          |
| RN-USR-15  | O IP do cliente é o da conexão; `X-Forwarded-For` só vale com `trusted-proxies > 0`                    | —    | `RateLimitingFilter`               |
| RN-USR-11  | `POST /user` só é público com `api.user-registration.public=true` (dev); em produção exige autenticação | 401 | `SecurityConfigurations` |
| RN-USR-10  | Usuário só edita/exclui **a si mesmo** (`PUT/DELETE /user` usam o usuário do token)     | —    | `UserController`                   |

## Pessoa física

| ID         | Regra                                                                                   | HTTP | Onde                               |
|------------|-----------------------------------------------------------------------------------------|------|------------------------------------|
| RN-PES-01  | Nome e e-mail são obrigatórios                                                          | 400  | `NaturalPerson.validate`           |
| RN-PES-02  | CPF, quando informado, é válido (`@CPF`), ≤ 11 caracteres e **único** entre ativos      | 400  | `Create/UpdateNaturalPersonService`|
| RN-PES-03  | RG ≤ 20 caracteres; telefone e celular ≤ 20 caracteres                                  | 400  | `NaturalPerson.validate`           |
| RN-PES-04  | Data de nascimento **não pode estar no futuro**                                         | 400  | `NaturalPerson.validate`           |
| RN-PES-05  | Ao criar, dispara e-mail de confirmação (token de **24 h**)                             | —    | `RequestEmailConfirmationService`  |
| RN-PES-06  | Pedir confirmação de e-mail já verificado, ou de e-mail inexistente, não faz nada       | 200  | `RequestEmailConfirmationService`  |
| RN-PES-07  | Gênero: `MALE`, `FEMALE` ou `OTHER`                                                     | 400  | `Gender`                           |

## Pessoa jurídica

| ID         | Regra                                                                                   | HTTP | Onde                               |
|------------|-----------------------------------------------------------------------------------------|------|------------------------------------|
| RN-EMP-01  | CNPJ obrigatório, válido (`@CNPJ`), armazenado **só com dígitos** (≤ 14) e **único**    | 400  | `Create/UpdateLegalPersonService`  |
| RN-EMP-02  | Razão social obrigatória                                                                | 400  | `LegalPerson.validate`             |
| RN-EMP-03  | Status de registro (`ACTIVE/INAPT/SUSPENDED/DOWN`) e da empresa (`ACTIVE/INACTIVE/BLOCKED`) obrigatórios | 400 | `LegalPerson.validate` |
| RN-EMP-04  | **Endereço é obrigatório**                                                              | 400  | `LegalPerson.validate`             |
| RN-EMP-05  | Representante é opcional; se informado, **nome é obrigatório** (≤ 255)                  | 400  | `Representative.validate`          |
| RN-EMP-06  | CNAE principal ≤ 20 caracteres                                                          | 400  | `LegalPerson.validate`             |

## Endereço

| ID         | Regra                                                                                   | HTTP | Onde                               |
|------------|-----------------------------------------------------------------------------------------|------|------------------------------------|
| RN-END-01  | CEP, cidade e estado são obrigatórios                                                   | 400  | `Address.validate`                 |
| RN-END-02  | Limites: CEP 20, tipo de logradouro 50, logradouro 255, número 20, ponto de referência 255, complemento 100, bairro 100, cidade 100, estado 50 | 400 | `Address.validate` |

## Grupo familiar

| ID         | Regra                                                                                   | HTTP | Onde                               |
|------------|-----------------------------------------------------------------------------------------|------|------------------------------------|
| RN-FAM-01  | Nome obrigatório (≤ 255) e **único**                                                    | 400  | `CreateFamilyGroupService`         |
| RN-FAM-02  | ID amigável `FAM-<n>` gerado por sequence; não editável                                 | 400  | `FamilyGroup.validate`             |
| RN-FAM-03  | Rendas e despesas não podem ser negativas                                               | 400  | `FamilyGroup.validate`             |
| RN-FAM-04  | Número de moradores ≥ 1 quando informado                                                | 400  | `FamilyGroup.validate`             |
| RN-FAM-05  | Vincular pessoa já vinculada à família                                                  | 409  | `AddNaturalPersonToFamilyGroupService` |
| RN-FAM-06  | Desvincular pessoa que não está na família                                              | 409  | `RemoveNaturalPersonFromFamilyGroupService` |
| RN-FAM-07  | Uma pessoa pode estar em **mais de uma** família (relação N–N)                          | —    | `family_group_natural_person`      |

## Programa

| ID         | Regra                                                                                   | HTTP | Onde                               |
|------------|-----------------------------------------------------------------------------------------|------|------------------------------------|
| RN-PRG-01  | Nome obrigatório e **único**                                                            | 400  | `Create/UpdateProgramService`      |
| RN-PRG-02  | Status obrigatório: `ACTIVE` ou `INACTIVE`                                              | 400  | `Program.validate`                 |
| RN-PRG-03  | Categoria de educador exige nome                                                        | 400  | `EducatorCategoryItem.validate`    |
| RN-PRG-04  | No update, categorias **ausentes da lista são removidas**; as com `id` são editadas; sem `id` são criadas | —  | `UpdateProgramService` |
| RN-PRG-05  | No update, categoria com `id` que **não é do programa** é rejeitada                     | 400  | `UpdateProgramService`             |
| RN-PRG-06  | Renomear o programa **recalcula o título** de todas as suas turmas                      | —    | `UpdateProgramService`             |
| RN-PRG-07  | Instituição do programa é opcional                                                      | —    | `Program`                          |

## Série escolar

| ID         | Regra                                                                                   | HTTP | Onde                               |
|------------|-----------------------------------------------------------------------------------------|------|------------------------------------|
| RN-SER-01  | Nome obrigatório (≤ 255) e **único** entre ativas                                       | 400  | `CreateSchoolGradeService`         |
| RN-SER-02  | Ordem (`gradeOrder`) obrigatória e **≥ 1**; define a ordenação no título das turmas     | 400  | `SchoolGrade.validate`             |
| RN-SER-03  | Abreviação ≤ 50 caracteres (opcional)                                                   | 400  | `SchoolGrade.validate`             |
| RN-SER-04  | Alterar ou excluir uma série **recalcula o título** das turmas que a usam               | —    | `Update/SoftDeleteSchoolGradeService` |

## Educando e responsáveis

| ID         | Regra                                                                                   | HTTP | Onde                               |
|------------|-----------------------------------------------------------------------------------------|------|------------------------------------|
| RN-LRN-01  | O educando e **todos** os responsáveis devem pertencer ao grupo familiar informado      | 400  | `Create/UpdateLearnerService`      |
| RN-LRN-02  | Uma pessoa física só pode ser educando uma vez                                          | 409  | `CreateLearnerService`             |
| RN-LRN-03  | O educando não pode ser responsável de si mesmo                                         | 400  | `Learner.validateResponsibleList`  |
| RN-LRN-04  | A mesma pessoa não pode ser responsável do mesmo educando duas vezes                    | 400  | `Learner.validateResponsibleList`  |
| RN-LRN-05  | Responsável exige tipo (`COMMUNICATION`/`FINANCIAL`) e parentesco                       | 400  | `Responsible.validate`             |
| RN-LRN-06  | Ano letivo entre 1900 e 2100                                                            | 400  | `Learner.validateAcademicYear`     |
| RN-LRN-07  | Não é possível trocar a pessoa física de um educando existente                          | 400  | `UpdateLearnerService`             |
| RN-LRN-08  | Novo educando nasce `ACTIVE`                                                            | —    | `Learner` (construtor)             |
| RN-LRN-09  | Desistir exige **motivo**; data padrão = hoje                                           | 400  | `DropoutLearnerCommand`, `LearnerStatusHistory` |
| RN-LRN-10  | Desistir de quem já desistiu                                                            | 409  | `Learner.dropOut`                  |
| RN-LRN-11  | Reativar quem não está desistente                                                       | 409  | `Learner.reactivate`               |
| RN-LRN-12  | Desistir **desativa todos os vínculos** do educando com turmas                          | —    | `DropoutLearnerService`            |
| RN-LRN-13  | Reativar **não** reativa os vínculos com turmas                                         | —    | `ReactivateLearnerService`         |
| RN-LRN-14  | Toda desistência/reativação grava histórico (`LearnerStatusHistory`)                    | —    | `Learner`                          |
| RN-LRN-15  | Papéis `LEARNER`/`RESPONSIBLE` da pessoa são mantidos automaticamente                   | —    | `LearnerRoleSynchronizer`          |
| RN-LRN-16  | `RESPONSIBLE` só é revogado quando a pessoa deixa de responder por **qualquer** educando | —   | `LearnerRoleSynchronizer`          |

## Colaborador

| ID         | Regra                                                                                   | HTTP | Onde                               |
|------------|-----------------------------------------------------------------------------------------|------|------------------------------------|
| RN-COL-01  | Pessoa física obrigatória e existente                                                   | 400/404 | `CreateCollaboratorService`     |
| RN-COL-02  | Uma pessoa física só pode ter **um** cadastro de colaborador                            | 409  | `CreateCollaboratorService`        |
| RN-COL-03  | Pessoa jurídica é opcional; se informada, precisa existir                               | 404  | `CreateCollaboratorService`        |
| RN-COL-04  | `isVoluntary` assume `false` quando não enviado                                         | —    | `CreateCollaboratorService`        |

## Turma

| ID         | Regra                                                                                   | HTTP | Onde                               |
|------------|-----------------------------------------------------------------------------------------|------|------------------------------------|
| RN-TUR-01  | Programa e instituição obrigatórios e existentes                                        | 400/404 | `CreateClassGroupService`       |
| RN-TUR-02  | Previsão de alunos obrigatória e ≥ 0                                                    | 400  | `ClassGroup.validate`              |
| RN-TUR-03  | Término = início + duração em meses; não pode ser anterior ao início                    | 400  | `ClassGroup`                       |
| RN-TUR-04  | Título é gerado: `Programa - Séries - Dias` (ver [turmas](fluxo-turmas.md#título-automático)) | 400  | `ClassGroup.composeTitle`          |
| RN-TUR-05  | Série inexistente na lista invalida a operação toda                                     | 400  | `loadSchoolGrades`                 |
| RN-TUR-06  | Horário: dia, início e fim obrigatórios; **fim > início**                               | 400  | `ClassSchedule.validate`           |
| RN-TUR-07  | Educando só uma vez por turma                                                           | 409  | `CreateClassLearnerService`        |
| RN-TUR-08  | Educador só uma vez por turma                                                           | 409  | `CreateClassCollaboratorService`   |
| RN-TUR-09  | A categoria do educador deve ser do **programa da turma**                               | 404  | `findByIdAndProgramIdOrThrow`      |
| RN-TUR-10  | Criar/editar/excluir horário recalcula o título                                         | —    | `*ClassScheduleService`            |
| RN-TUR-11  | Excluir turma remove (soft) horários, educadores, educandos e séries associadas         | —    | `SoftDeleteClassGroupService`      |
| RN-TUR-12  | Editar turma: campos `null` preservam o valor atual (exceto `note`)                     | —    | `ClassGroup.Updater`               |

## Exclusão e cascatas

Tudo é **soft delete**. A tabela mostra o que cada exclusão arrasta — e, em **negrito**, o que **não** é
verificado e pode deixar referências "órfãs".

| Ao excluir…        | Também some                                              | **Não é verificado / não some**                                              |
|--------------------|----------------------------------------------------------|------------------------------------------------------------------------------|
| **Turma**          | Horários, educadores, educandos (vínculos), séries da turma | —                                                                          |
| **Educando**       | Responsáveis dele; papéis `LEARNER`/`RESPONSIBLE` órfãos | **Vínculos com turmas** (`ClassLearner`) continuam                           |
| **Grupo familiar** | Só o grupo                                               | **Educandos e responsáveis da família continuam**; pessoas continuam         |
| **Pessoa física**  | Só a pessoa                                              | **Educando, colaborador, responsabilidade, vínculos com família continuam**  |
| **Pessoa jurídica**| Só a empresa (e seu endereço, por cascade)               | **Programas, turmas, educandos e colaboradores que a referenciam continuam** |
| **Programa**       | Só o programa                                            | **Suas turmas continuam** (e o título delas não é recalculado)               |
| **Série escolar**  | Só a série                                               | Turmas são atualizadas no título, mas **educandos/responsáveis seguem com `schoolGradeId` apontando para ela** |
| **Colaborador**    | Só o colaborador                                         | **Vínculos com turmas continuam**                                            |
| **Usuário**        | Só o usuário (soft delete)                               | Refresh tokens continuam no banco até expirar (e não há revogação)           |

> Antes de
> adicionar "proteção ao excluir" (409 se houver dependentes), decida a política **com a ONG** e registre
> como ADR — afeta toda a operação.

## Mensagens de erro

Formato único (`ErrorResponse`):

```json
{
  "code": "Bad Request",
  "message": "A pessoa física informada não pertence a este grupo familiar.",
  "errors": [ { "field": "...", "message": "..." } ]
}
```

`errors` só aparece em falha de validação de campos (`@Valid`). Mensagens são em português e escritas
para serem exibidas diretamente ao usuário.
