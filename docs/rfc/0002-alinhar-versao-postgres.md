# 0002. Alinhar versão do PostgreSQL entre dev e testes

Data: 2026-09-29
Status: Proposta
Autor: Documentação inicial do projeto

## Problema

O projeto roda **duas versões diferentes de PostgreSQL** e ninguém decidiu qual é a correta.

| Onde | Versão | Fonte |
| --- | --- | --- |
| Ambiente local (`docker-compose.yml`) | `postgres:15-alpine` | serviço `db` |
| Testes de integração (Testcontainers) | `postgres:16-alpine` | `PostgresIntegrationTest` |
| Produção | desconhecido | não documentado |

O teste de integração valida contra a 16, o dev trabalha contra a 15, e a produção contra o que
for. Isso significa que **a suíte verde não garante que a aplicação funcione no banco de
desenvolvimento**, e vice-versa.

Não é teórico. A diferença entre 15 e 16 já apareceu de forma concreta: a migration `V011` usa
`COALESCE` e houve um erro `COALESCE types text and integer cannot be matched` ao aplicar schema
com migration obsoleta, justamente na fronteira de versão e dialeto.

## Contexto

A [RFC 0001](0001-autorizacao-por-role.md) trata de segurança. Esta trata de consistência de
ambiente, e é mais barata de resolver.

O que pondera:

- **15** é o piso do que o time já usa localmente hoje, em Docker Compose.
- **16** é o que os testes usam desde a adoção de Testcontainers
  ([ADR 007](../adr/007-testcontainers-postgres.md)).
- A 16 entrega melhorias reais de performance de executor, registro em log, e
  `VACUUM` mais barato. A 17 já está no mercado.
- `pg_dump` da 17 é compatível com 15 e 16; a 16 não é retrocompatível para 15.

O que não consta do repositório: a versão de produção. Isso precisa ser confirmado com quem
opera antes de fixar qualquer padrão.

## Opções

### Opção A — Padronizar em 16

Sobe o `docker-compose.yml` para `postgres:16-alpine`.

**Prós**

- Dev e teste passam a usar exatamente a mesma versão, que é o objetivo.
- A 16 é a versão mais nova em uso no projeto hoje.
- `PostgresIntegrationTest` não muda.

**Contras**

- Devs com o volume antigo do Compose sobem um Postgres 15 sobre diretório de dados da 16 e
  o banco não abre. Exige `docker compose down -v` e recriação, o que **apaga os dados locais**.
  Isso precisa ser comunicado, porque perder o banco local de alguém sem aviso é chato.

### Opção B — Padronizar em 15

Baixa o `PostgresIntegrationTest` para `postgres:15-alpine`.

**Prós**

- Ninguém perde dado local: o Compose continua igual.
- A 15 é estável e suficiente para o caso.

**Contras**

- Perde os ganhos da 16 sem motivo: testar numa versão que ninguém usa de verdade é testar o
  cenário errado.
- Exige recriar a imagem de teste e revalidar a suíte, já que a 15 não tem os mesmos dialetos.

### Opção C — Extrair a versão para uma variável única

Uma propriedade ou variável de ambiente define a versão, e tanto o Compose quanto o
Testcontainers leem.

**Prós**

- Elimina a classe de bug, não só o sintoma: a versão não pode mais divergir porque há um lugar
  só.
- Permite testar contra outra versão sem editar código.

**Contras**

- `docker-compose.yml` lê de `.env` por padrão, o que é mais um arquivo para o dev entender.
- Variável de ambiente para algo que deveria ser fixo é um pouco de indireção.
- Não resolve a questão de fundo: ainda é preciso escolher qual versão.

## Recomendação

**Opção A somada à Opção C**, nesta ordem: definir 16 como o padrão do projeto, extrair a
versão para um ponto único, e só então alinhar o Compose.

Justificativa: a 16 já é o padrão de fato dos testes, é a mais nova em uso real, e é a major
atual suportada. A opção C vem junto porque é a que impede a divergência de voltar — o
`PostgresIntegrationTest` e o `docker-compose.yml` passam a citar a mesma variável, e não podem
divergir por descuido.

A perda de dados locais da opção A é resolvida com aviso no README e na documentação, e é custo
único, de uma vez.

Antes de implementar, é obrigatório **confirmar a versão de produção**. Se a produção estiver
na 15 e não puder subir, a recomendação muda para a opção B, e a variável fica como
documentação do porquê da divergência temporária.

## Impacto

- **Código:** `PostgresIntegrationTest` e `docker-compose.yml` passam a usar a versão definida
  em um ponto único, provavelmente `POSTGRES_VERSION` no `.env` com default `16-alpine`.
- **Banco:** nenhuma migration nova. Mas devs precisam recriar o volume local, e o
  `docker/database/init.sql` precisa rodar de novo, já que recomeça a sequence do
  `friendlyId`.
- **API:** nenhuma mudança.
- **Testes:** a suíte roda contra a imagem nova na primeira execução. Como a definição deixa de
  estar fixa no código, vale um teste que falhe se a variável não estiver definida, para não
  sobrar `null` na tag da imagem.
- **Docs:** o [ADR 007](../adr/007-testcontainers-postgres.md) cita `postgres:16-alpine`
  explicitamente e precisará de nota; `docs/banco-de-dados.md` descreve a sequência do Compose e
  precisa refletir o novo passo de recriação de volume; e o `AGENTS.md` tem a tabela de versões
  que hoje já registra a divergência e deve ser enxugada para uma versão só.
- **Fora do repositório:** confirmar a versão de produção com o time de operação.

## Decisão

Pendente. Bloqueada até confirmar a versão de produção.
