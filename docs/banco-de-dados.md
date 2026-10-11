# Banco de dados

Migrations, nomenclatura SQL e ambiente local. Para o porquê, veja
[ADR 003](adr/003-gerenciamento-banco-dados.md).

PostgreSQL é o único banco suportado. **Não existe H2 no projeto** — nem em produção, nem nos
testes (ver [ADR 007](adr/007-testcontainers-postgres.md)).

## Flyway é o dono do schema

`ddl-auto=validate` em `application.properties`: o Hibernate **confere** o schema, nunca altera.
Alterar estrutura é responsabilidade exclusiva do Flyway.

```properties
spring.jpa.hibernate.ddl-auto=validate
spring.flyway.enabled=true
spring.flyway.baseline-on-migrate=true
```

Se as entidades e o banco divergirem, a aplicação **não sobe** com `Schema-validation` apontando
a coluna faltando. Isso é intencional: melhor falhar no boot do que quebrar em produção.

## Migrations existentes

Toda a história do schema está em `src/main/resources/db/migration/`. A lista completa é sempre
a desse diretório — não é mantida aqui para não ficar desatualizada a cada migration nova.

## Criar uma migration

Nome no formato `V{sequencial}__{descricao_em_snake_case}.sql`, com três dígitos e dois
underscores:

```bash
src/main/resources/db/migration/V012_add_xxx_to_yyy.sql
```

Exemplo de conteúdo:

```sql
ALTER TABLE family_group
    ADD COLUMN observation VARCHAR(500);
```

## Regras que quebram o build se ignoradas

### Nunca edite uma migration já aplicada

O Flyway valida o checksum de cada script. Editar uma migration já aplicada causa
`Validate failed: migration checksum mismatch` no próximo boot, em todos os ambientes.

O que fazer: edite e **recrie o banco local** (`docker compose down -v && docker compose up -d`), e
se a migration já foi para produção, crie uma nova migration que corrija.

Isso vale mesmo para "só corrigir um typo no nome de coluna". Para consertar uma migration já
aplicada em ambiente compartilhado, o caminho é uma migration nova.

### Nunca use `ddl-auto=create` ou `update`

Isso faria o Hibernate alterar o schema por conta própria, divergindo do Flyway. `validate` apenas.

### Nunca altere o banco manualmente

Não use pgAdmin ou DBeaver para mudar tabela em ambiente que other devs usam. A alteração precisa
estar em migration versionada, senão o próximo `./mvnw test` ou o próximo deploy quebra.

## Nomenclatura SQL

| Objeto            | Padrão                       | Exemplo                        |
|-------------------|------------------------------|--------------------------------|
| Tabela            | `snake_case` plural          | `family_group_natural_person`  |
| Coluna            | `snake_case`                 | `birth_date`, `deleted_at`     |
| Chave primária    | `id`                         | `id`                           |
| Chave estrangeira | `fk_{origem}_{destino}`      | `fk_family_group_address`      |
| Índice            | `idx_{tabela}_{coluna}`      | `idx_natural_person_cpf`       |
| Unique            | `uq_{tabela}_{coluna}`       | `uq_user_email`                |
| Sequence          | `{dominio}_{finalidade}_seq` | `family_group_friendly_id_seq` |

Colunas de auditoria, herdadas de `AuditableEntity`, são padrão em todas as tabelas de domínio:

| Coluna         | Tipo          | Papel                               |
|----------------|---------------|-------------------------------------|
| `date_created` | `timestamptz` | Criação, preenchida pelo Hibernate. |
| `date_updated` | `timestamptz` | Última alteração.                   |
| `deleted_at`   | `timestamptz` | Soft delete. `NULL` significa vivo. |

A coluna `deleted_at` só ganha efeito automático se a entidade tiver `@SQLRestriction`. Confira
antes de filtrar à mão.

## Sequences

IDs são gerados em Java com `UUID`, mas identificadores de negócio que precisam ser sequenciais e
legíveis usam sequence do Postgres. Exemplo: o `friendlyId` do grupo familiar (`FAM-1`, `FAM-2`).

A sequence é criada na migration:

```sql
CREATE SEQUENCE IF NOT EXISTS family_group_friendly_id_seq START WITH 1;
```

E consumida com `nextval`, que é atômico:

```java

@Query(value = "select nextval('family_group_friendly_id_seq')", nativeQuery = true)
Long nextFriendlyIdValue();
```

**Sequence pode deixar buraco** em caso de rollback, e isso é aceitável para identificador de
display. Não use sequence onde o número precisar ser sequencial sem buraco (contabilidade, número
de documento oficial). Para os testes, lembre que `nextval` não reverte com rollback de transação:
ver [`testes.md`](testes.md).

## Ambiente local

```bash
docker compose up -d
```

O `docker-compose.yml` sobe três serviços em cadeia:

1. `db` — PostgreSQL 15 em `localhost:5432`, banco `projetoresgatedb`, usuário `admin/admin`.
2. `migrations` — container do Flyway que aplica as migrations e **sai com sucesso**.
3. `seeder` — roda `docker/database/init.sql` só depois que as migrations concluem.

O `depends_on` com `condition: service_completed_successfully` garante a ordem. Depois de
`up -d`, confira que os serviços `migrations` e `seeder` terminaram com exit 0:

```bash
docker compose ps -a
docker compose logs seeder
```

Se qualquer um falhar, `up -d` vai continuar pois `db` já está de pé, mas o schema ou os dados de
exemplo ficam errados. Nesses casos, `docker compose down -v && docker compose up -d` recria do
zero.

O `psql` do seeder roda com `ON_ERROR_STOP=1`: um erro no `init.sql` derruba o container em vez de
ser engolido. Sem essa flag o `psql` imprime o erro no log e **sai com exit 0**, e o
`service_completed_successfully` enxerga sucesso — é assim que um seed pela metade passa despercebido.
O `init.sql` é idempotente: num bloco `DO` cada `INSERT` usa `ON CONFLICT DO NOTHING` (ou
`NOT EXISTS`, onde a tabela não tem restrição única, como `user_roles`). Rodar o seeder de novo
sobre um banco já populado não duplica linhas nem falha — inclusive quando o banco foi semado por
uma versão antiga do script, que parava no primeiro `admin@projetoresgate.com` encontrado. Esse
comportamento vale para todo o script, não só para o usuário administrador.

O `init.sql` popula os dados de exemplo de todos os domínios — usuários, pessoas físicas,
endereços, instituições, programas, grupos familiares e membros, séries escolares, educandos,
responsáveis e os papéis `LEARNER`/`RESPONSIBLE` de `natural_person_role` — e faz `setval` na
sequence do `friendlyId` para que o próximo grupo criado no ambiente local continue a numeração do
seed (o valor aplicado é o maior entre o maior `FAM-<n>` já existente e o último `last_value` da
sequence, então o setval também cobre cadastros feitos pela API). Como o script inteiro é um único
bloco `DO`, qualquer erro faz rollback de tudo — ou o seed inteiro entra, ou nada entra.

## Variáveis de ambiente

A aplicação não roda sem elas. Vêm do seu shell ou do run configuration:

```properties
DB_URL=jdbc:postgresql://localhost:5432/projetoresgatedb
DB_USERNAME=admin
DB_PASSWORD=admin
API_SECURITY_TOKEN_SECRET=<segredo JWT>
CORS_ALLOWED-ORIGINS=http://localhost:3000
MAIL_USERNAME=<email>
MAIL_PASSWORD=<senha de app>
```

Nunca comite essas variáveis. `API_SECURITY_TOKEN_SECRET` em especial.
