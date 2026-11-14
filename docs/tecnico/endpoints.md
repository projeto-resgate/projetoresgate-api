# Mapa de endpoints

Visão de conjunto das **89 rotas** da API. O contrato detalhado (campos, exemplos, códigos de resposta) é
gerado pelo Swagger e é a **fonte de verdade**:

- Local: `http://localhost:8080/swagger-ui.html`
- JSON OpenAPI: `/v3/api-docs`

Este documento serve para **achar a rota certa rapidamente** e lembrar das exceções. Se divergir do Swagger,
vale o Swagger.

## Convenções

| Item              | Regra                                                                                              |
|-------------------|----------------------------------------------------------------------------------------------------|
| Autenticação      | `Authorization: Bearer <accessToken>` em tudo, **exceto** as rotas marcadas 🔓                      |
| IDs               | UUID nas rotas (`/{id}`)                                                                           |
| Criar             | `POST` → `201 Created` (com `Location` em alguns) e corpo                                          |
| Editar            | `PUT /{id}` → `200` com o recurso; o `id` vem **da URL**, nunca do corpo                           |
| Excluir           | `DELETE /{id}` → `204 No Content` (soft delete)                                                    |
| Listar            | `GET` paginado + filtros dinâmicos ([ADR 009](../adr/009-busca-com-specification-builder.md))      |
| Autocomplete      | `GET /autocomplete?searchTerm=&limit=` — leve, para campos de busca                                |
| Nomes             | `GET /names` — lista enxuta `{id, name}` para preencher `<select>`                                 |
| Erros             | `{code, message, errors[]}` — ver [regras](regras-implementadas.md#mensagens-de-erro)                 |

> Nenhuma rota verifica **papel** (só se há usuário autenticado). Ver
> [RFC 0001](../rfc/0001-autorizacao-por-role.md).

## Usuário e sessão — `/user`

| Método | Rota                     | Descrição                                             |
|--------|--------------------------|-------------------------------------------------------|
| POST   | `/user` 🔓               | Cria usuário (⚠️ público, nasce ADMIN)                |
| POST   | `/user/login` 🔓         | Login; devolve access token e seta cookie de refresh  |
| POST   | `/user/refresh` 🔓       | Renova a sessão (usa o cookie)                        |
| POST   | `/user/forgot-password` 🔓 | Envia e-mail de recuperação                         |
| POST   | `/user/reset-password` 🔓  | Define nova senha com o token                       |
| POST   | `/user/logout`           | Encerra esta sessão                                   |
| POST   | `/user/logout-all`       | Encerra todas as sessões                              |
| GET    | `/user/{id}`             | Busca usuário                                         |
| PUT    | `/user`                  | Edita **o próprio** usuário (nome, apelido)           |
| DELETE | `/user`                  | Exclui **o próprio** usuário                          |

Fluxos: [`autenticacao.md`](autenticacao.md).

## Pessoa física — `/natural-person`

| Método | Rota                                    | Descrição                                    |
|--------|-----------------------------------------|----------------------------------------------|
| POST   | `/natural-person` 🔓                    | Cadastra e dispara e-mail de confirmação     |
| PUT    | `/natural-person/{id}`                  | Edita                                        |
| DELETE | `/natural-person/{id}`                  | Exclui (soft)                                |
| GET    | `/natural-person`                       | Lista com filtros                            |
| GET    | `/natural-person/{id}`                  | Detalhe                                      |
| GET    | `/natural-person/autocomplete`          | Busca rápida por nome, apelido, CPF ou RG (parcial) |
| POST   | `/natural-person/request-email-confirmation` 🔓 | Reenvia confirmação                  |
| POST   | `/natural-person/confirm-email/{token}` 🔓      | Confirma e-mail                      |

## Pessoa jurídica — `/legal-person`

| Método | Rota                        | Descrição            |
|--------|-----------------------------|----------------------|
| POST   | `/legal-person`             | Cadastra             |
| PUT    | `/legal-person/{id}`        | Edita                |
| DELETE | `/legal-person/{id}`        | Exclui (soft)        |
| GET    | `/legal-person`             | Lista com filtros    |
| GET    | `/legal-person/{id}`        | Detalhe              |
| GET    | `/legal-person/autocomplete`| Busca rápida         |

## Grupo familiar — `/family-group`

| Método | Rota                                                  | Descrição                                          |
|--------|-------------------------------------------------------|----------------------------------------------------|
| POST   | `/family-group`                                       | Cria família (gera `FAM-n`)                        |
| PUT    | `/family-group/{id}`                                  | Edita                                              |
| DELETE | `/family-group/{id}`                                  | Exclui (soft)                                      |
| GET    | `/family-group`                                       | Lista com filtros                                  |
| GET    | `/family-group/{id}`                                  | Detalhe                                            |
| GET    | `/family-group/{id}/natural-person`                   | Membros da família (com papéis `LEARNER`/`RESPONSIBLE`) |
| POST   | `/family-group/{id}/natural-person`                   | **Cria** pessoa nova já vinculada                  |
| POST   | `/family-group/{id}/natural-person/{naturalPersonId}` | Vincula pessoa **existente**                       |
| DELETE | `/family-group/{id}/natural-person/{naturalPersonId}` | Desvincula (não exclui a pessoa)                   |

## Educando — `/learner`

| Método | Rota                                | Descrição                                                      |
|--------|-------------------------------------|----------------------------------------------------------------|
| POST   | `/learner`                          | Cria educando com responsáveis                                 |
| PUT    | `/learner/{id}`                     | Edita; **substitui** a lista de responsáveis                   |
| POST   | `/learner/{id}/dropout`             | Registra desistência (motivo obrigatório)                      |
| POST   | `/learner/{id}/reactivate`          | Reativa                                                        |
| DELETE | `/learner/{id}`                     | Exclui (soft) + responsáveis                                   |
| GET    | `/learner`                          | Lista com filtros                                              |
| GET    | `/learner/{id}`                     | Detalhe (inclui responsáveis; **não** inclui o histórico de status) |
| GET    | `/learner/autocomplete`             | Busca rápida                                                   |
| GET    | `/learner/eligible-responsibles`    | Pessoas da família elegíveis a responsável (`familyGroupId`, `excludeNaturalPersonId`) |

Fluxos: [`fluxo-educandos.md`](fluxo-educandos.md).

## Colaborador — `/collaborator`

| Método | Rota                          | Descrição         |
|--------|-------------------------------|-------------------|
| POST   | `/collaborator`               | Cadastra          |
| PUT    | `/collaborator/{id}`          | Edita             |
| DELETE | `/collaborator/{id}`          | Exclui (soft)     |
| GET    | `/collaborator`               | Lista             |
| GET    | `/collaborator/{id}`          | Detalhe           |
| GET    | `/collaborator/autocomplete`  | Busca rápida      |

## Programa — `/program`

| Método | Rota                                         | Descrição                                       |
|--------|----------------------------------------------|-------------------------------------------------|
| POST   | `/program`                                   | Cria com categorias de educador                 |
| PUT    | `/program/{id}`                              | Edita; **reconcilia** categorias (cria/edita/remove) |
| DELETE | `/program/{id}`                              | Exclui (soft)                                   |
| GET    | `/program`                                   | Lista                                           |
| GET    | `/program/names`                             | Nomes para `<select>`                           |
| GET    | `/program/{id}`                              | Detalhe                                         |
| GET    | `/program/{id}/educator-category-items/names`| Categorias do programa (para vincular educador) |

## Série escolar — `/school-grade`

| Método | Rota                      | Descrição                 |
|--------|---------------------------|---------------------------|
| POST   | `/school-grade`           | Cria                      |
| PUT    | `/school-grade/{id}`      | Edita (recalcula títulos) |
| DELETE | `/school-grade/{id}`      | Exclui (recalcula títulos)|
| GET    | `/school-grade`           | Lista                     |
| GET    | `/school-grade/names`     | Nomes para `<select>`     |
| GET    | `/school-grade/{id}`      | Detalhe                   |

## Turma — `/class-group`

| Método | Rota                                        | Descrição                                   |
|--------|---------------------------------------------|---------------------------------------------|
| POST   | `/class-group`                              | Cria                                        |
| PUT    | `/class-group/{id}`                         | Edita (parcial)                             |
| DELETE | `/class-group/{id}`                         | Exclui (cascata nos vínculos)               |
| GET    | `/class-group`                              | Lista resumida                              |
| GET    | `/class-group/{id}`                         | Detalhe                                     |
| POST   | `/class-group/{classGroupId}/schedule`      | Cria horário                                |
| PUT    | `/class-group/{classGroupId}/schedule/{id}` | Edita horário                               |
| GET    | `/class-group/{classGroupId}/schedule`      | Lista horários                              |
| DELETE | `/class-group/{classGroupId}/schedule/{id}` | Exclui horário                              |
| POST   | `/class-group/{classGroupId}/learner`       | Vincula educando                            |
| GET    | `/class-group/{classGroupId}/learner`       | Lista educandos da turma                    |
| DELETE | `/class-group/{classGroupId}/learner/{id}`  | Remove vínculo (`id` = id do **vínculo**)   |
| POST   | `/class-group/{classGroupId}/collaborator`  | Vincula educador                            |
| PUT    | `/class-group/{classGroupId}/collaborator/{id}` | Edita categoria/ativo                   |
| GET    | `/class-group/{classGroupId}/collaborator`  | Lista educadores                            |
| DELETE | `/class-group/{classGroupId}/collaborator/{id}` | Remove vínculo                          |

> Nos sub-recursos o `{id}` é o **id do vínculo** (`ClassLearner`, `ClassCollaborator`, `ClassSchedule`),
> não o id do educando/colaborador. O service valida que o vínculo pertence à turma da URL.

Fluxos: [`fluxo-turmas.md`](fluxo-turmas.md).

## Operação

| Rota                         | Descrição                                                             |
|------------------------------|-----------------------------------------------------------------------|
| `/actuator/health`           | Saúde da aplicação (**exige autenticação** na config atual)           |
| `/actuator/prometheus`       | Métricas Prometheus (**exige autenticação** na config atual)          |
| `/actuator/info`             | Informações                                                           |

Ver [operação](../operacao/runbook.md).
