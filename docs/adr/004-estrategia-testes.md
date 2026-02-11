# 4. Estratégia de Testes

Data: 2026-02-10

## Contexto

Testes automatizados são a garantia de que novas funcionalidades não quebram as antigas (regressão). Para desenvolvedores iniciantes, a dúvida comum é "o que testar?" e "como testar?". Testar tudo da mesma forma pode ser ineficiente e lento.

## Decisão

Adotamos a **Pirâmide de Testes**, focando em uma base sólida de testes unitários e testes de integração estratégicos.

### 1. Testes Unitários (Foco: Regra de Negócio)
*   **Onde:** Camadas de `usecase`, `service` e `domain`.
*   **Ferramentas:** JUnit 5, Mockito.
*   **Objetivo:** Testar a lógica de negócio isoladamente.
*   **Como:**
    *   Mockar todas as dependências externas (Repositories, outros Services).
    *   Testar caminhos felizes (sucesso).
    *   Testar caminhos tristes (exceções, validações falhando).
    *   Não subir o contexto do Spring (`@SpringBootTest` é proibido aqui, use apenas `@ExtendWith(MockitoExtension.class)`). Isso garante execução rápida.

### 2. Testes de Integração (Foco: Contrato e Fluxo)
*   **Onde:** Camada de `api` (Controllers).
*   **Ferramentas:** `@SpringBootTest`, `MockMvc`, Testcontainers (opcional para banco).
*   **Objetivo:** Garantir que a API recebe e retorna os dados corretamente (HTTP Status, JSON Body) e que os componentes se conversam.
*   **Como:**
    *   Usar `MockMvc` para simular requisições HTTP.
    *   Verificar se o Controller chama o UseCase corretamente.
    *   Verificar se o tratamento de exceção (`GlobalExceptionHandler`) está funcionando (ex: retornar 404 quando o Service lança exceção).

### 3. Cobertura
Embora não tenhamos um número rígido, buscamos cobrir os fluxos principais de negócio. Classes de configuração, DTOs simples (getters/setters) e código gerado não precisam de testes intensivos.

## O que NÃO fazer

*   Não escreva testes que dependem de dados pré-existentes no banco (o teste deve criar seus dados e limpar depois, ou usar mocks).
*   Não use `System.out.println` para validar testes; use `Assertions` (`assertEquals`, `assertThrows`).
*   Não ignore testes falhando. Se falhou, o build deve quebrar.

## Exemplo

**Teste Unitário (Service):**
```java
@ExtendWith(MockitoExtension.class)
class CreateUserServiceTest {
    @Mock UserRepository repository;
    @InjectMocks CreateUserService service;

    @Test
    void shouldCreateUser() {
        // Arrange
        User user = new User("Diego", "email@teste.com");
        when(repository.save(any())).thenReturn(user);

        // Act
        User created = service.execute(new CreateUserCommand(...));

        // Assert
        assertNotNull(created);
        verify(repository).save(any());
    }
}
```
