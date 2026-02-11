# 5. Padrões de Nomenclatura e Idioma

Data: 2026-02-10

## Contexto

Em projetos com múltiplos desenvolvedores, a falta de padronização na nomenclatura e no idioma do código gera confusão e dificulta a leitura. Misturar inglês e português, ou usar nomes pouco descritivos, torna o código difícil de manter.

## Decisão

Adotamos o **Inglês** como idioma padrão para o código e o **Português** para a documentação e comentários.

### 1. Idioma do Código
*   **Classes, Interfaces, Métodos, Variáveis:** Sempre em **Inglês**.
    *   Ex: `UserController`, `findUserById`, `birthDate`.
    *   Não use: `UsuarioController`, `buscarUsuarioPorId`, `dataNascimento`.
*   **Pacotes:** Sempre em minúsculas, sem separadores (`com.projetoresgate.api`).
*   **Constantes:** `UPPER_SNAKE_CASE` (ex: `MAX_LOGIN_ATTEMPTS`).

### 2. Idioma da Documentação
*   **JavaDoc:** Em **Português**.
    *   Ex: `/** Busca um usuário pelo ID. */`
*   **Comentários de Código:** Em **Português**.
    *   Ex: `// Valida se o email já existe no banco`
*   **Mensagens de Commit:** Em **Português** (imperativo).
    *   Ex: `Adiciona validação de CPF`, `Corrige bug no login`.
*   **ADRs e README:** Em **Português**.

### 3. Sufixos Obrigatórios
Para facilitar a identificação das responsabilidades das classes, usamos sufixos:

*   **Controller:** `XController` (ex: `UserController`)
*   **Service:** `XService` (ex: `UserService`)
*   **Repository:** `XRepository` (ex: `UserRepository`)
*   **UseCase:** `XUseCase` (ex: `CreateUserUseCase`)
*   **Command:** `XCommand` (ex: `CreateUserCommand`)
*   **Query:** `XQuery` (ex: `FindUserQuery`)
*   **DTO (Request/Response):** `XRequest`, `XResponse` (ex: `CreateUserRequest`, `UserResponse`)
*   **Exception:** `XException` (ex: `UserNotFoundException`)
*   **Mapper:** `XMapper` (ex: `UserMapper`)

### 4. Nomes Descritivos
Evite abreviações obscuras. O nome deve revelar a intenção.
*   **Ruim:** `u`, `usr`, `d`, `calc`.
*   **Bom:** `user`, `currentUser`, `daysSinceLastLogin`, `calculateTotal`.

## O que NÃO fazer

*   Não misture idiomas na mesma linha (`val dataBirth = ...`).
*   Não use nomes genéricos demais (`Manager`, `Processor`, `Helper`) sem um contexto claro.
*   Não use prefixos de tipo (`strName`, `iCount`). O tipo já diz o que é.

## Exemplo

**Correto:**
```java
/**
 * Serviço responsável por criar um novo usuário.
 */
@Service
public class CreateUserService implements CreateUserUseCase {
    private final UserRepository userRepository;

    public User execute(CreateUserCommand command) {
        // Verifica se o email já está em uso
        if (userRepository.existsByEmail(command.email())) {
            throw new UserAlreadyExistsException("Email já cadastrado.");
        }
        // ...
    }
}
```
