# 2. Estratégia de Tratamento de Erros (Error Handling)

Data: 2026-02-10

## Contexto

Em uma API REST, é crucial que os erros sejam retornados de forma consistente e informativa para o cliente (frontend/mobile). Desenvolvedores iniciantes frequentemente cometem erros como:
1.  Engolir exceções (`catch (Exception e) {}`), deixando o sistema em estado inconsistente sem avisar ninguém.
2.  Retornar código HTTP 200 (OK) mesmo quando ocorre um erro, colocando a mensagem de erro no corpo da resposta.
3.  Expor stack traces (detalhes técnicos do erro) para o cliente, o que é uma falha de segurança.

## Decisão

Adotamos uma estratégia de **Tratamento Global de Exceções** centralizada.

### 1. GlobalExceptionHandler
Utilizamos a classe `GlobalExceptionHandler` (anotada com `@ControllerAdvice` ou `@RestControllerAdvice`) localizada na camada de `infrastructure`. Ela intercepta todas as exceções não tratadas que sobem dos Controllers.

### 2. Mapeamento de Exceções
As exceções de negócio (lançadas nos UseCases/Services) devem ser mapeadas para códigos HTTP semanticamente corretos:

*   **400 Bad Request:** Erros de validação de dados (ex: campo obrigatório faltando, formato de email inválido).
*   **401 Unauthorized:** Falha na autenticação (token inválido ou ausente).
*   **403 Forbidden:** Usuário autenticado, mas sem permissão para acessar o recurso.
*   **404 Not Found:** Recurso não encontrado (ex: `UserNotFoundException`).
*   **409 Conflict:** Conflito de estado (ex: tentar cadastrar email já existente).
*   **422 Unprocessable Entity:** Regra de negócio violada (ex: tentar aprovar um pedido já aprovado).
*   **500 Internal Server Error:** Erros inesperados (NullPointerException, falha no banco). O cliente deve receber uma mensagem genérica ("Ocorreu um erro interno"), e o erro real deve ser logado.

### 3. Estrutura da Resposta de Erro
A API deve retornar um JSON padronizado em caso de erro. Recomendamos o uso do padrão **Problem Details for HTTP APIs (RFC 7807)** ou um DTO customizado simples contendo:
*   `timestamp`: Data/hora do erro.
*   `status`: Código HTTP.
*   `error`: Descrição curta do erro.
*   `message`: Mensagem detalhada para o desenvolvedor do frontend.
*   `path`: Endpoint que gerou o erro.

## O que NÃO fazer

*   **NUNCA** faça `try-catch` silencioso. Se você capturar uma exceção, ou trate-a (recuperação) ou relance-a (para o GlobalHandler pegar).
*   **NUNCA** retorne `null` quando algo der errado. Lance uma exceção.
*   **NUNCA** exponha dados sensíveis na mensagem de erro.

## Exemplo

**Errado (no Service):**
```java
public User findUser(Long id) {
    try {
        return repository.findById(id).get();
    } catch (Exception e) {
        return null; // O Controller vai receber null e pode dar erro depois
    }
}
```

**Correto (no Service):**
```java
public User findUser(Long id) {
    return repository.findById(id)
        .orElseThrow(() -> new ResourceNotFoundException("Usuário não encontrado com id: " + id));
}
```
O `GlobalExceptionHandler` capturará `ResourceNotFoundException` e retornará 404 automaticamente.
