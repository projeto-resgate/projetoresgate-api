# 3. Gerenciamento de Banco de Dados e Migrations (Flyway)

Data: 2026-02-10

## Contexto

Em projetos com múltiplos desenvolvedores, a consistência do esquema do banco de dados (tabelas, colunas, índices) é crítica. Alterações manuais (via pgAdmin/DBeaver) ou o uso do Hibernate (`ddl-auto=update`) em produção podem causar:
1.  Perda de dados.
2.  Inconsistência entre ambientes (Dev vs Prod).
3.  Dificuldade em rastrear quem fez qual alteração e quando.

## Decisão

Adotamos o **Flyway** para gerenciar todas as alterações no esquema do banco de dados.

### 1. Versionamento de Scripts
Toda alteração no banco deve ser feita através de um script SQL versionado, localizado em `src/main/resources/db/migration`.

*   **Prefixo:** `V` (Versionado)
*   **Versão:** Número sequencial ou timestamp (ex: `V1__`, `V2__`, `V202405221030__`).
*   **Separador:** `__` (dois underscores).
*   **Descrição:** Nome descritivo da alteração (ex: `create_table_users`, `add_column_cpf`).
*   **Extensão:** `.sql`

**Exemplo:** `V1__create_table_users.sql`

### 2. Imutabilidade
Uma vez que um script de migração foi aplicado (rodou com sucesso), ele **NUNCA** deve ser alterado. Se precisar corrigir algo, crie um novo script de migração (`V2__fix_column_type.sql`).

### 3. Hibernate DDL Auto
A propriedade `spring.jpa.hibernate.ddl-auto` deve ser configurada como `validate` em produção e `none` ou `validate` em desenvolvimento, para garantir que o Hibernate apenas verifique se as entidades Java batem com o banco, mas não altere nada automaticamente. O Flyway é o único dono da estrutura do banco.

### 4. Nomenclatura no Banco
*   **Tabelas e Colunas:** Use `snake_case` (minúsculas com sublinhado). Ex: `user_profiles`, `birth_date`.
*   **Chaves Estrangeiras:** `fk_{tabela_origem}_{tabela_destino}`.
*   **Índices:** `idx_{tabela}_{coluna}`.

## O que NÃO fazer

*   **NUNCA** altere a estrutura do banco manualmente em qualquer ambiente compartilhado.
*   **NUNCA** delete um script de migração que já foi comitado.
*   **NUNCA** use `ddl-auto=create` ou `update` em produção.

## Fluxo de Trabalho

1.  Desenvolvedor precisa adicionar uma coluna `phone` na tabela `users`.
2.  Cria arquivo `src/main/resources/db/migration/V5__add_phone_to_users.sql`.
3.  Conteúdo: `ALTER TABLE users ADD COLUMN phone VARCHAR(20);`.
4.  Roda a aplicação localmente. O Flyway detecta o novo arquivo e aplica a mudança.
5.  Testa e commita o arquivo `.sql`.
6.  Outros devs puxam o código, rodam a aplicação e o banco deles é atualizado automaticamente.
