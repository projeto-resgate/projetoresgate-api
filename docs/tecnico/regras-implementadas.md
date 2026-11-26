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
`PRG` programa · `SER` série · `LRN` educando · `COL` colaborador · `TUR` turma · `GER` geral.

## Regras gerais

| ID         | Regra                                                                                   | HTTP | Onde                               |
|------------|-----------------------------------------------------------------------------------------|------|------------------------------------|
| RN-GER-01  | Dados com máscara são gravados **só com o valor**: ao criar e editar, a API aceita com ou sem máscara e remove `.`, `-`, `/`, `(`, `)` e espaços de CPF, RG (também em maiúsculas), CNPJ, CNAE, CEP, telefone e celular (inclusive do representante). Letras não são removidas, então valor inválido continua sendo recusado. A **exibição com máscara é do front** | — | `MaskUtils`, comandos e `validate()` das entidades |
| RN-GER-02  | A migration V020 limpou os valores já gravados; CPF/RG cuja limpeza criaria duplicidade com outro registro ativo foram mantidos como estavam | — | `V020__Remove_masks_from_stored_values.sql` |
| RN-GER-03  | Buscas, filtros e autocompletes **não tratam máscara**: o termo é comparado como foi enviado. Como os valores são gravados limpos, o front deve enviar CPF, RG, CNPJ, CEP e telefones **sem máscara** ao buscar | — | Serviços de listagem e autocomplete |
| RN-GER-05  | **Não se exclui o que está em uso.** `DELETE` de programa, série escolar, pessoa jurídica, pessoa física, grupo familiar, colaborador e turma é recusado se ainda houver dependentes **ativos**, com a lista do que impede: *"Não é possível excluir o programa porque existem vínculos com: 2 turmas. Remova os vínculos antes de excluir."*. Dependentes já excluídos não contam. Ver [Exclusão](#exclusão-e-cascatas) | 409 | `DeletionGuard` e `countDependencies`/`count*` dos repositórios |
| RN-GER-04  | **PUT recebe o mesmo corpo do POST e se comporta igual**: substitui o registro inteiro. Campo obrigatório no POST é obrigatório no PUT (400); campo opcional ausente fica **nulo**; lista ausente vale como **lista vazia**; `active` ausente vale o padrão da criação (`true`). Exceções: `PUT /user` não altera e-mail e só troca a senha quando `password` (com `currentPassword`) é enviado; o vínculo de educador com a turma não troca o colaborador | 400 | Comandos `Update*Command` e serviços de edição |

## Usuário e acesso

| ID         | Regra                                                                                   | HTTP | Onde                               |
|------------|-----------------------------------------------------------------------------------------|------|------------------------------------|
| RN-USR-01  | E-mail do usuário é único entre usuários ativos, **sem diferenciar maiúsculas**; é gravado sem espaços nas pontas e em minúsculas (V021 converteu os existentes), e login e "esqueci a senha" também não diferenciam maiúsculas. O e-mail de um usuário excluído pode ser reutilizado | 400  | `User`, `CreateUserService`, `UserRepository.findByEmailIgnoreCase` |
| RN-USR-02  | E-mail e nome do usuário são obrigatórios                                               | 400  | `User.validate`                    |
| RN-USR-03  | Todo usuário criado nasce com o papel `ADMIN`                                           | —    | `User` (construtor)                |
| RN-USR-04  | Login com e-mail inexistente ou senha errada devolve a **mesma** resposta               | 401  | `AuthenticateUserService`          |
| RN-USR-05  | Access token dura 15 min; refresh token dura 7 dias e **rotaciona** a cada uso; refresh ausente, inválido ou expirado devolve 401 | 401  | `TokenService`, `RefreshTokenServiceImpl` |
| RN-USR-06  | `logout-all` revoga todos os refresh tokens **e** invalida os access tokens (`tokenVersion`) | — | `LogoutAllService`                 |
| RN-USR-07  | Reset de senha: token de 10 min, uso único, senha nova com **≥ 6 caracteres**           | 400  | `ResetPasswordService`             |
| RN-USR-08  | `forgot-password` responde 200 sempre (não revela se o e-mail existe)                   | 200  | `RequestPasswordResetService`      |
| RN-USR-09  | Login, refresh, forgot-password e reset-password limitados a **5 req/min por IP** (camada AUTH)       | 429  | `RateLimitingFilter`               |
| RN-USR-10  | Usuário só edita/exclui **a si mesmo** (`PUT/DELETE /user` usam o usuário do token)     | —    | `UserController`                   |
| RN-USR-11  | `POST /user` só é público com `api.user-registration.public=true` (dev); em produção exige autenticação | 401 | `SecurityConfigurations` |
| RN-USR-12  | Rotas públicas de escrita (criar usuário/pessoa, confirmar e-mail) limitadas a **20 req/min por IP** (PUBLIC) | 429 | `RateLimitingFilter`            |
| RN-USR-13  | Demais rotas limitadas a **300 req/min por IP** (GENERAL); preflight `OPTIONS` não conta                | 429  | `RateLimitingFilter`               |
| RN-USR-14  | Mesmo destinatário recebe no máximo **3 e-mails/hora** (reset e confirmação); excedido, ignora em silêncio, sem erro | — | `EmailRateLimiter`          |
| RN-USR-15  | O IP do cliente é o da conexão; `X-Forwarded-For` só vale com `trusted-proxies > 0`                    | —    | `RateLimitingFilter`               |

## Pessoa física

| ID         | Regra                                                                                   | HTTP | Onde                               |
|------------|-----------------------------------------------------------------------------------------|------|------------------------------------|
| RN-PES-01  | Nome e e-mail são obrigatórios                                                          | 400  | `NaturalPerson.validate`           |
| RN-PES-02  | CPF, quando informado, é válido (`@CPF`), ≤ 11 caracteres e **único** entre ativos      | 400  | `Create/UpdateNaturalPersonService`|
| RN-PES-03  | RG ≤ 20 caracteres; telefone e celular ≤ 20 caracteres                                  | 400  | `NaturalPerson.validate`           |
| RN-PES-04  | Data de nascimento **não pode estar no futuro**                                         | 400  | `NaturalPerson.validate`           |
| RN-PES-05  | Ao criar, dispara e-mail de confirmação (token de **24 h**)                             | —    | `RequestEmailConfirmationService`  |
| RN-PES-06  | `request-email-confirmation` envia um link a **cada** pessoa ainda não verificada com aquele e-mail (sem diferenciar maiúsculas); e-mail inexistente ou já verificado não faz nada | 200  | `RequestEmailConfirmationService`  |
| RN-PES-07  | Gênero: `MALE`, `FEMALE` ou `OTHER`                                                     | 400  | `Gender`                           |
| RN-PES-08  | RG, quando informado, aceita pontuação, prefixo de UF e dígito `X` (ex.: `12.345.678-X`, `MG-12.345.678`) e é **único** entre ativos; mesma validação ao criar e editar. Pode ser o próprio CPF (CIN) | 400 | `RGValidator`, `Create/UpdateNaturalPersonService` |
| RN-PES-09  | E-mail **pode se repetir** entre pessoas (ex.: criança com o e-mail do responsável)     | —    | `NaturalPerson`                    |
| RN-PES-10  | Trocar o e-mail desfaz a verificação e envia nova confirmação ao e-mail novo (mudar só maiúsculas/minúsculas não conta) | — | `NaturalPerson.Updater`, `UpdateNaturalPersonService` |
| RN-PES-11  | A resposta traz `emailVerified`                                                         | —    | `NaturalPersonResponse`            |
| ~~RN-PES-12~~ | ~~O filtro `cpf=` e `cellphone=` aceitam valor com pontuação~~ (substituída pela RN-GER-03) | — | — |
| RN-PES-13  | Campos opcionais enviados vazios (`""`) — apelido, RG, CPF, telefone e celular — são tratados como não informados | — | `Create/UpdateNaturalPersonCommand` |
| ~~RN-PES-14~~ | ~~A busca por RG ignora a pontuação~~ (substituída pela RN-GER-03) | — | — |

## Pessoa jurídica

| ID         | Regra                                                                                   | HTTP | Onde                               |
|------------|-----------------------------------------------------------------------------------------|------|------------------------------------|
| RN-EMP-01  | CNPJ obrigatório, válido (`@CNPJ`), gravado **sem máscara** (≤ 14) e **único** entre ativas | 400  | `Create/UpdateLegalPersonService`  |
| RN-EMP-02  | Razão social obrigatória                                                                | 400  | `LegalPerson.validate`             |
| RN-EMP-03  | Status de registro (`ACTIVE/INAPT/SUSPENDED/DOWN`) e da empresa (`ACTIVE/INACTIVE/BLOCKED`) obrigatórios | 400 | `LegalPerson.validate` |
| RN-EMP-04  | **Endereço é obrigatório**                                                              | 400  | `LegalPerson.validate`             |
| RN-EMP-05  | Representante é opcional; se informado, **nome é obrigatório** (≤ 255)                  | 400  | `Representative.validate`          |
| RN-EMP-06  | CNAE principal ≤ 20 caracteres                                                          | 400  | `LegalPerson.validate`             |
| RN-EMP-07  | Nome exibido (autocomplete, turmas, educandos, colaboradores, local de trabalho): nome de exibição; se vazio, nome fantasia; se vazio, razão social | — | `LegalPerson.getName` |

## Endereço

| ID         | Regra                                                                                   | HTTP | Onde                               |
|------------|-----------------------------------------------------------------------------------------|------|------------------------------------|
| RN-END-01  | CEP, cidade e estado são obrigatórios                                                   | 400  | `Address.validate`                 |
| RN-END-02  | Limites: CEP 20, tipo de logradouro 50, logradouro 255, número 20, ponto de referência 255, complemento 100, bairro 100, cidade 100 | 400 | `Address.validate` |
| RN-END-03  | CEP com **8 dígitos**, gravado sem máscara (`89010-000` → `89010000`)                  | 400  | `Address.validate`                 |
| RN-END-04  | Estado é a **sigla da UF** (27 opções); aceita minúsculas e grava em maiúsculas          | 400  | `Address.validate`                 |

## Grupo familiar

| ID         | Regra                                                                                   | HTTP | Onde                               |
|------------|-----------------------------------------------------------------------------------------|------|------------------------------------|
| RN-FAM-01  | Nome obrigatório (≤ 255) e **único** entre ativos, sem diferenciar maiúsculas/minúsculas | 400  | `CreateFamilyGroupService`         |
| RN-FAM-02  | ID amigável `FAM-<n>` gerado por sequence; não editável                                 | 400  | `FamilyGroup.validate`             |
| RN-FAM-03  | Rendas e despesas não podem ser negativas                                               | 400  | `FamilyGroup.validate`             |
| RN-FAM-04  | Número de moradores ≥ 1 quando informado                                                | 400  | `FamilyGroup.validate`             |
| RN-FAM-05  | Vincular pessoa já vinculada à família                                                  | 409  | `AddNaturalPersonToFamilyGroupService` |
| RN-FAM-06  | Desvincular pessoa que não está na família                                              | 409  | `RemoveNaturalPersonFromFamilyGroupService` |
| RN-FAM-07  | Uma pessoa pode estar em **mais de uma** família (relação N–N)                          | —    | `family_group_natural_person`      |
| RN-FAM-08  | Não dá para desvincular um **educando** do seu único grupo familiar ativo (vincule-o a outro antes) | 409 | `RemoveNaturalPersonFromFamilyGroupService` |
| RN-FAM-09  | Não dá para desvincular quem é **responsável** por um educando daquela família (tire-o da lista de responsáveis antes) | 409 | `RemoveNaturalPersonFromFamilyGroupService` |
| RN-FAM-10  | Excluir a família só é permitido **sem pessoas vinculadas** (RN-GER-05); o endereço é excluído junto | 409 | `SoftDeleteFamilyGroupService` |
| RN-FAM-11  | No PUT, endereço ausente deixa a família **sem endereço** (o endereço anterior é excluído) | — | `UpdateFamilyGroupService` |

## Programa

| ID         | Regra                                                                                   | HTTP | Onde                               |
|------------|-----------------------------------------------------------------------------------------|------|------------------------------------|
| RN-PRG-01  | Nome obrigatório e **único** entre ativos, sem diferenciar maiúsculas/minúsculas        | 400  | `Create/UpdateProgramService`      |
| RN-PRG-02  | Status obrigatório: `ACTIVE` ou `INACTIVE`                                              | 400  | `Program.validate`                 |
| RN-PRG-03  | Categoria de educador exige nome                                                        | 400  | `EducatorCategoryItem.validate`    |
| RN-PRG-04  | No update, a lista `educatorCategoryItems` substitui as categorias: as com `id` são editadas, as sem `id` são criadas e as ausentes são removidas (lista ausente = remover todas, respeitando a RN-PRG-08). O `institutionId` também é substituído (ausente = sem instituição) | — | `UpdateProgramService` |
| RN-PRG-05  | No update, categoria com `id` que **não é do programa** é rejeitada                     | 400  | `UpdateProgramService`             |
| RN-PRG-06  | Renomear o programa **recalcula o título** de todas as suas turmas                      | —    | `UpdateProgramService`             |
| RN-PRG-07  | Instituição do programa é opcional                                                      | —    | `Program`                          |
| RN-PRG-08  | Categoria vinculada a educadores de turmas **não pode ser removida**                    | 409  | `UpdateProgramService`             |
| RN-PRG-09  | O programa não pode ter duas categorias com o mesmo nome (sem diferenciar maiúsculas)   | 400  | `EducatorCategoryNames`            |

## Série escolar

| ID         | Regra                                                                                   | HTTP | Onde                               |
|------------|-----------------------------------------------------------------------------------------|------|------------------------------------|
| RN-SER-01  | Nome obrigatório (≤ 255) e **único** entre ativas, sem diferenciar maiúsculas/minúsculas | 400  | `CreateSchoolGradeService`         |
| RN-SER-02  | Ordem (`gradeOrder`) obrigatória e **≥ 1**; define a ordenação no título das turmas     | 400  | `SchoolGrade.validate`             |
| RN-SER-03  | Abreviação ≤ 50 caracteres (opcional)                                                   | 400  | `SchoolGrade.validate`             |
| RN-SER-04  | **Renomear** uma série recalcula o título das turmas que a usam. Série usada por turma, educando ou responsável **não pode ser excluída** (RN-GER-05) | 409 | `UpdateSchoolGradeService`, `SoftDeleteSchoolGradeService` |

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
| RN-LRN-13  | Reativar **sem** `classGroupId` não reativa vínculos; **com** `classGroupId` cria/ativa só o vínculo daquela turma | — | `ReactivateLearnerService` |
| RN-LRN-14  | Toda desistência/reativação grava histórico (`LearnerStatusHistory`)                    | —    | `Learner`                          |
| RN-LRN-15  | Papéis `LEARNER`/`RESPONSIBLE` da pessoa são mantidos automaticamente                   | —    | `LearnerRoleSynchronizer`          |
| RN-LRN-16  | `RESPONSIBLE` só é revogado quando a pessoa deixa de responder por **qualquer** educando | —   | `LearnerRoleSynchronizer`          |
| RN-LRN-17  | Educando ativo reativado **com turma** só ativa o vínculo: sem erro e sem novo histórico | —   | `ReactivateLearnerService`         |
| RN-LRN-18  | Não existe exclusão de educando (`DELETE /learner/{id}` foi removido)                   | —    | `LearnerController`                |
| RN-LRN-19  | `GET /learner` devolve `status` calculado: `ACTIVE` (turma em andamento), `NO_CLASS`, `DROPPED_OUT`; o filtro `status` usa os mesmos valores | — | `FindAllLearnersService` |
| RN-LRN-20  | Histórico de desistências e retomadas consultável em `GET /learner/{id}/status-history`, mais recente primeiro | 404 | `FindLearnerStatusHistoryService` |
| RN-LRN-21  | Vincular à turma (inclusive pela reativação) exige a pessoa física do educando existente | 404 | `ActivateClassLearnerLinkService`, `CreateClassLearnerService` |
| RN-LRN-22  | A data da desistência não pode estar no futuro                                          | 400  | `DropoutLearnerCommand`            |
| RN-LRN-23  | A busca (`searchTerm`) de `GET /learner` também encontra pelo CPF (enviado sem máscara)   | —    | `FindAllLearnersService`           |

## Colaborador

| ID         | Regra                                                                                   | HTTP | Onde                               |
|------------|-----------------------------------------------------------------------------------------|------|------------------------------------|
| RN-COL-01  | Pessoa física obrigatória e existente                                                   | 400/404 | `CreateCollaboratorService`     |
| RN-COL-02  | Uma pessoa física só pode ter **um** cadastro de colaborador                            | 409  | `CreateCollaboratorService`        |
| RN-COL-03  | Pessoa jurídica é opcional; se informada, precisa existir                               | 404  | `CreateCollaboratorService`        |
| RN-COL-04  | `isVoluntary` é obrigatório (criar e editar)                                            | 400  | `Create/UpdateCollaboratorCommand` |
| RN-COL-05  | A busca e o autocomplete de colaborador encontram por nome, apelido, CPF, RG, razão social, nome fantasia, nome de exibição e CNPJ da empresa (documentos enviados sem máscara) | — | `CollaboratorRepository.searchRows` |

## Turma

| ID         | Regra                                                                                   | HTTP | Onde                               |
|------------|-----------------------------------------------------------------------------------------|------|------------------------------------|
| RN-TUR-01  | Programa e instituição obrigatórios e existentes                                        | 400/404 | `CreateClassGroupService`       |
| RN-TUR-02  | Previsão de alunos obrigatória e ≥ 0                                                    | 400  | `ClassGroup.validate`              |
| RN-TUR-03  | Término = início + duração em meses; não pode ser anterior ao início                    | 400  | `ClassGroup`                       |
| RN-TUR-04  | Título é gerado: `Programa - Séries - Dias` (ver [turmas](fluxo-turmas.md#título-automático)) | 400  | `ClassGroup.composeTitle`          |
| RN-TUR-05  | Série inexistente na lista invalida a operação toda                                     | 400  | `loadSchoolGrades`                 |
| RN-TUR-06  | Horário: dia, início e fim obrigatórios; **fim > início**                               | 400  | `ClassSchedule.validate`           |
| RN-TUR-07  | Educando só uma vez por turma, mesmo com o vínculo inativo                              | 409  | `CreateClassLearnerService`        |
| RN-TUR-08  | Educador só uma vez por turma; a pessoa física do colaborador precisa existir           | 409/404 | `CreateClassCollaboratorService` |
| RN-TUR-09  | A categoria do educador deve ser do **programa da turma**                               | 404  | `findByIdAndProgramIdOrThrow`      |
| RN-TUR-10  | Criar/editar/excluir horário recalcula o título                                         | —    | `*ClassScheduleService`            |
| RN-TUR-11  | Excluir turma só é permitido **sem educandos nem educadores vinculados** (RN-GER-05); horários e séries vão junto | 409  | `SoftDeleteClassGroupService`      |
| ~~RN-TUR-12~~ | ~~Editar turma: campos `null` preservam o valor atual~~ (substituída pela RN-GER-04) | — | — |

## Exclusão e cascatas

Tudo é **soft delete**: o registro continua no banco com `deleted_at` preenchido.

### Regra: não se exclui o que está em uso (RN-GER-05)

Antes de excluir, o service conta os dependentes **ativos** (com `deleted_at` nulo). Se houver algum, a exclusão é
recusada com **409** e a mensagem lista o que impede, para quem está na tela saber o que desvincular antes.

| Ao excluir…        | É bloqueado se houver…                                                                 | Vai junto (partes do próprio registro) |
|--------------------|----------------------------------------------------------------------------------------|----------------------------------------|
| **Programa**       | turmas                                                                                 | categorias de educador                 |
| **Série escolar**  | turmas, educandos ou responsáveis com essa série                                       | —                                      |
| **Pessoa jurídica**| turmas (instituição), programas, colaboradores, educandos (instituição) ou pessoas físicas (local de trabalho) | endereço           |
| **Pessoa física**  | cadastro de educando, responsabilidade por educando, cadastro de colaborador ou grupo familiar | —                              |
| **Grupo familiar** | pessoas físicas no grupo                                                               | endereço                               |
| **Colaborador**    | vínculos de educador em turmas (ativos ou inativos)                                    | —                                      |
| **Turma**          | educandos ou educadores vinculados (ativos ou inativos)                                | horários e séries da turma             |
| **Educando**       | — (não existe exclusão de educando; só desistência)                                    | —                                      |
| **Usuário**        | — (sem dependentes; exclui só o próprio usuário)                                       | —                                      |

Para excluir algo em uso, desfaça os vínculos primeiro: remova o educando/educador da turma
(`DELETE /class-group/{id}/learner|collaborator/{id}`), tire a pessoa do grupo
(`DELETE /family-group/{id}/natural-person/{naturalPersonId}`), troque o programa/instituição das turmas, etc.

Como funciona no código: `shared/deletion/DeletionGuard` monta a mensagem e lança `IllegalStateException` (409). Cada
repositório tem uma consulta nativa de contagem (`countDependencies`, `countActiveClassGroups`, `countActiveClassLinks`,
`countActiveMembers`), em SQL para que `identity` não precise importar classes de `academic`. Testes:
`DeletionDependenciesIntegrationTest` (consultas no Postgres) e os `SoftDelete*ServiceTest`.

### Leitura

Como nada em uso pode ser excluído, **não existem registros órfãos** e não há código especial para eles: toda
leitura usa os métodos normais (`findById`, `findAllById`), que respeitam o `@SQLRestriction("deleted_at IS NULL")`
das entidades. Criar ou trocar um vínculo para um registro excluído responde **404**, como para um id inexistente.
Autocompletes e seletores também não mostram excluídos.

CPF, RG, CNPJ e e-mail de usuário são únicos **só entre registros ativos** (índices parciais, V019): é possível
recadastrar o documento de um registro excluído.

Testes: `DeletionDependenciesIntegrationTest`, `DeletedParentPolicyIntegrationTest` e `DeletionConsistencyIntegrationTest`
(banco real).

## Mensagens de erro

Formato único (`ErrorResponse`):

```json
{
  "code": "Bad Request",
  "message": "A pessoa física informada não pertence a este grupo familiar.",
  "errors": [ { "field": "...", "message": "..." } ]
}
```

`errors` só aparece em falha de validação de campos (`@Valid`) ou em JSON com valor de tipo errado (o campo
vem em `field`). Mensagens são em português e escritas para serem exibidas diretamente ao usuário.

| Situação                                                              | HTTP |
|-----------------------------------------------------------------------|------|
| JSON mal formatado, enum/data/número inválido no corpo                | 400  |
| Parâmetro de rota ou query com tipo errado (UUID, número, enum)       | 400 (enum lista os valores aceitos) |
| Parâmetro obrigatório ausente                                         | 400  |
| Endereço inexistente (com token válido)                               | 404  |
| Método HTTP não suportado                                             | 405  |
| `Content-Type` diferente de JSON                                      | 415  |
| Sem token ou token inválido                                           | 401  |
