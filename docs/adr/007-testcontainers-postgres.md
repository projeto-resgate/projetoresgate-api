---
name: testcontainers-postgres
status: aceito
summary: Todo teste de banco estende PostgresIntegrationTest com Flyway real. H2 proibido.
---

# 7. Testcontainers com PostgreSQL real

## Decisão

**Adotar Testcontainers com `postgres:16-alpine` como banco de integração de dados, e remover o
H2 do projeto.**

Toda classe de teste que toca banco segue:

```java

@DataJpaTest
class MeuRepositoryTest extends PostgresIntegrationTest { ...
}
```

A classe base fica em `src/test/java/.../shared/testcontainers/PostgresIntegrationTest.java` e:

1. sobe um `PostgreSQLContainer` como campo `static` (iniciado uma vez por JVM);
2. conecta via `@DynamicPropertySource` injetando url, usuário e senha;
3. usa `@AutoConfigureTestDatabase(replace = Replace.NONE)`, para o Spring não substituir o
   datasource pelo embedded;
4. deixa o Flyway ligado, aplicando as migrations de verdade, sem `ddl-auto=create-drop`.

O container é **estático e compartilhado** entre todas as classes de integração. Subir um por
classe deixaria a suíte lenta demais. A limpeza fica a cargo do Ryuk, que o Testcontainers sobe
junto e encerra junto com a JVM.

## Por quê

O projeto usa PostgreSQL com recursos que o H2 não reproduz, ou reproduz diferente:

- `sequence` nativa com `nextval` (o `friendlyId` do grupo familiar depende disso);
- `on conflict do nothing` em query nativa de vínculo;
- `@SQLRestriction` do Hibernate, que gera SQL específico;
- JPQL com `join` interno e `count` para paginação;
- tipos e constraints que só existem no Postgres.

O resultado é que os testes passavam no H2 e quebravam em produção, ou o inverso: alguém escrevia
SQL específico de Postgres, o teste passava no H2 por acidente, e o bug só aparecia no ambiente
real. H2 também não exercita as migrations, então o schema dos testes nunca era o de produção.

O [ADR 004](../adr/004-estrategia-testes.md) chegou a listar Testcontainers como "opcional", mas
nunca foi adotado.

## Consequências

### Positivas

- O schema dos testes é o de produção, com as migrations aplicadas de verdade. Quebrar uma
  migration quebra o teste.
- Dialeto, sequence, `@SQLRestriction` e paginação com join passam a ser validados no banco certo.
- Nenhum SQL pode "passar por acaso" num banco diferente.
- Sem a dependência do H2, um teste novo que esqueça de estender a base quebra alto e visível, em
  vez de silenciosamente rodar em outro banco.

### Negativas

- Precisa de Docker para rodar `./mvnw test`. Em ambiente sem Docker, os 3 testes de integração
  falham, e a suíte inteira não passa.
- Mais lento. Um teste de integração que rodava em ~0,7s no H2 passou para ~21s com o container
  subindo, contando o pull da imagem. Na primeira execução do CI o pull adiciona ~10s.
- `nextval` não reverte com rollback de transação, então teste que consome sequence precisa
  reiniciar explicitamente com `ALTER SEQUENCE ... RESTART WITH 1`.

### Neutras

- A dependência `com.h2database:h2` foi removida do `pom.xml`.
- Os 377 testes continuam verdes, sem reescrita de lógica.

## Como ficou o custo

O custo de tempo é Acceptado porque a alternativa (H2) gerava confiança falsa — testes que passam sem
validar nada. A dependência de Docker é assumida: o projeto já exige Docker Compose para ambiente
local, então a exigência não é nova para quem desenvolve nele.

## O que NÃO fazer

- **Não reintroduzir H2** para "acelerar" a suíte. Se a lentidão incomodar, o caminho é marcar as
  classes de integração com um profile e rodá-las separado, não trocar o banco.
- **Não usar `ddl-auto=create-drop` em teste de integração.** O schema tem que vir do Flyway.
- **Não criar um `PostgreSQLContainer` por classe.** O custo de subir o container é o que torna
  a suíte lenta; reaproveitar o estático é o que a mantém viável.
- **Não confiar em `nextval` transacional.** Não é. Reinicie a sequence no `@BeforeEach` quando o
  teste depende do valor.
