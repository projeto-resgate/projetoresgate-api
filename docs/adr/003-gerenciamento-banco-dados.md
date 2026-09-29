---
name: banco-de-dados
status: aceito
summary:
  Flyway é o único dono do schema e ddl-auto=validate. Nunca edite migration já commitada: crie a próxima.
---

# 3. Banco de dados e migrations

## Decisão

O Flyway é o único dono da estrutura do banco. Toda alteração de schema é um arquivo SQL
versionado em `src/main/resources/db/migration`.

O Hibernate nunca altera o schema. `spring.jpa.hibernate.ddl-auto=validate` em todos os
perfis: ele só confere que as entidades Java batem com o banco e falha se não baterem.

## Como escrever uma migration

Nome no formato `V<NNN>__<descricao>.sql`, com três dígitos e dois underscores.

```
V001__Initial_setup.sql
V011__Create_family_groups_tables.sql
V012__add_phone_to_natural_person.sql
```

O Flyway aplica em ordem numérica, uma vez, e guarda o que já rodou em `flyway_schema_history`.
Existem 11 migrations, `V001` a `V011`.

## Regra que não tem exceção

**Nunca edite uma migration que já foi commitada.** Se o SQL está errado, crie `V012` corrigindo.
Editar uma migration já aplicada não muda o banco de quem já rodou, e quebra o histórico sem
avisar. O histórico de um banco é append-only.

Para consertar algo localmente durante o desenvolvimento, o banco de teste do Testcontainers é
recriado a cada execução, então basta apagar e deixar o Flyway rodar de novo.

## Nomenclatura

- **Tabelas e colunas:** `snake_case`. Entidade Java `FamilyGroup` mapeia para `family_group`, via
  `@Table(name = "family_group")` explícito.
- **Tabela de junção:** `family_group_natural_person`.
- **Chave estrangeira:** `REFERENCES <tabela>(id)` declarada inline na coluna, sem constraint
  nomeada. Veja `V011__Create_family_groups_tables.sql`.
- **Índice:** `idx_<tabela>_<coluna>`.

Índice de busca textual usa a extensão `pg_trgm`:

```sql
CREATE EXTENSION IF NOT EXISTS pg_trgm;
CREATE INDEX IF NOT EXISTS idx_natural_person_cpf_trgm ON natural_person USING GIN (cpf gin_trgm_ops);
```

É o que faz `~` (ILIKE) não fazer full scan.

## O que NÃO fazer

- Não alterar o banco manualmente em ambiente compartilhado. O próximo `git pull` desfaz.
- Não usar `ddl-auto=create` ou `update`. `validate` em todos os perfis.
- Não deletar migration commitada.
- Não escrever migration que depende de dado de teste. Migration de teste não existe: o
  Testcontainers sobe o Postgres vazio e o Flyway aplica tudo do zero, o que é exatamente o que
  você quer que aconteça.

## Script para o dia a dia

```bash
# ver o que o Flyway aplicou
./mvnw -o flyway:info

# validar os SQL sem rodar a aplicação
./mvnw -o test -Dtest=PostgresIntegrationTestSuite
```

A referência completa, incluindo como o schema dos testes é montado, está em
[`banco-de-dados.md`](../banco-de-dados.md).
