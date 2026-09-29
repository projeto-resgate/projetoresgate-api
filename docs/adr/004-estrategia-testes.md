---
name: estrategia-de-testes
status: aceito
summary: Unitário=@ExtendWith(MockitoExtension), controller=@WebMvcTest, banco=@DataJpaTest extends PostgresIntegrationTest. Nunca @SpringBootTest nem H2.
---

# 4. Estratégia de Testes

## Decisão

Três tipos de teste, e o tipo é escolhido pelo que a dúvida precisa responder. A suíte tem 375
testes em 69 classes.

### 1. Testes Unitários (Foco: Regra de Negócio)
*   **Onde:** Camadas de `usecase`, `service` e `domain`.
*   **Ferramentas:** JUnit 5, Mockito.
*   **Objetivo:** Testar a lógica de negócio isoladamente.
*   **Como:**
    *   Mockar todas as dependências externas (Repositories, outros Services).
    *   Testar caminhos felizes (sucesso).
    *   Testar caminhos tristes (exceções, validações falhando).
    *   Não subir o contexto do Spring (`@SpringBootTest` é proibido aqui, use apenas `@ExtendWith(MockitoExtension.class)`). Isso garante execução rápida.

### 2. Testes de Controller (Foco: Contrato HTTP)
*   **Onde:** Camada de `api`.
*   **Ferramentas:** `@WebMvcTest` com `MockMvc`. Os use cases são `@MockBean`.
*   **Objetivo:** Garantir que a API recebe e devolve os dados certos: status HTTP, JSON de saída,
  header `Location`, e o mapeamento de exceção.
*   **Como:**
    *   `@WebMvcTest` carrega só a camada web. Não usar `@SpringBootTest`: sobe o contexto
      inteiro sem necessidade e deixa a suíte lenta.
    *   Sempre importar a configuração de segurança com `@Import(SecurityConfigurations.class)`.
      Sem isso a requisição volta 403 e o teste falha sem causa aparente.
    *   Usar `@WithMockCustomUser` para simular o usuário autenticado.
    *   Verificar também os campos que **não** devem aparecer no JSON, com
      `jsonPath(...).doesNotExist()`.

### 3. Testes de Integração de Dados (Foco: Banco Real)
*   **Onde:** Camada de `repository` e services que tocam banco.
*   **Ferramentas:** `@DataJpaTest` + `extends PostgresIntegrationTest`, que sobe um PostgreSQL real
  via Testcontainers e aplica as migrations do Flyway.
*   **Objetivo:** Validar o que só o banco de verdade revela: JPQL e query nativa, paginação com
  `join`, `@SQLRestriction`, sequence e constraint.
*   **Como:**
    *   Todo teste que toca banco **precisa** do Testcontainers. Não há H2 no projeto: ele não
      reproduz o dialeto do Postgres, então o teste passaria sem validar nada. Ver [ADR 007](007-testcontainers-postgres.md).
    *   Criar os dados que o teste precisa. Não depender do seed.
    *   Lembrar que `nextval` não reverte com o rollback do teste, então sequence usada pelo teste
      precisa ser reiniciada no `@BeforeEach`.

### 4. Cobertura

Sem número rígido. Cobrem-se os fluxos de negócio. Classe de configuração, DTO simples e código
gerado não precisam de teste.

## O que NÃO fazer

*   **Não use `@SpringBootTest`** sem necessidade. Ele sobe o contexto inteiro e deixa a suíte
    lenta sem testar nada a mais que `@WebMvcTest` e `@DataJpaTest` já não cubram.
*   **Não reintroduzir H2.** Não está no `pom.xml` e não valida o dialeto do Postgres.
*   **Não use `ddl-auto=create-drop` em teste de integração.** O schema tem que vir do Flyway,
    senão o teste roda contra um schema que ninguém usa.
*   **Não escreva teste que só verifica mock.** Confirmar que o Mockito devolveu o que você mandou
    não testa nada. Asserte o comportamento.
*   **Não dependa de dados pré-existentes.** O teste cria o que precisa.
*   **Não use `System.out.println`** para validar; use `Assertions`.
*   **Não deixe teste falhando.** Se falhou, o build quebra e a causa precisa ser resolvida.

## Onde ver os exemplos

Cada tipo de teste tem exemplo completo, com os detalhes de `MockMvc`, `@WithMockCustomUser`,
Testcontainers e reset de sequence, em [`docs/testes.md`](../testes.md).
