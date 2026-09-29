---
name: estrutura-do-projeto
status: aceito
summary: Pasta por feature, não por camada. Domínio não importa nada de fora; service em service/, nunca usecase/impl/.
---

# 1. Estrutura do projeto

## Decisão

O código se divide em `core` (o domínio) e `infrastructure` (o que conversa com o framework e
com o mundo externo). Dentro de `core`, o agrupamento é por **feature** (`user`, `naturalperson`,
`familygroup`, `program`), não por camada técnica.

```
core/{feature}/
  api/           *Controller.java
  api/dto/       *Command.java (entrada) e *Response.java (saída)
  usecase/       *UseCase.java (interface)
  usecase/command/  *Command.java  (objeto de caso de uso)
  usecase/query/    *Query.java    (objeto de caso de uso)
  service/       *Service.java (implementa o UseCase)
  domain/        *Entity.java, enums
  repository/    *Repository.java

infrastructure/
  config/         configuração de framework
  security/       SecurityConfigurations, filtros
  handler/        GlobalExceptionHandler, ErrorResponse
  exception/      exceções de negócio
  email/          envio de e-mail
  services/       CookieService, TokenService (tarefas que não são regra de negócio)
  utils/          utilitários

shared/
  entity/         AuditableEntity (createdAt, updatedAt)
  specification/  SpecificationBuilder, GenericSpecification, SearchCriteria
  validation/     anotações e validadores de validação
```

## Contexto

A regra "por feature, não por camada" resolve o problema de um `controller/` com trinta
controllers e um `service/` com trinta services: você lê uma pasta e vê o assunto inteiro.
Mudar um cadastro de pessoa toca seis arquivos, e eles ficam juntos.

## Regras

- **Dependência aponta para dentro.** `api` e `service` conhecem `domain`. `domain` não conhece
  ninguém: nem `api`, nem `service`, nem `repository`.
- **Repository só acessa a própria feature.** Se `naturalperson` precisa de `familygroup`, isso é
  escopo novo, não um import cruzado silencioso.
- **O service implementa o UseCase.** Não existe `usecase/impl/`.
- **Entidade não é anêmica.** Regra de negócio mora em `create(...)` e `update()...apply()`, não no
  service.
