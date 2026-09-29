---
name: tratamento-de-erros
status: aceito
summary: Sobe exceção, o GlobalExceptionHandler decide o status. ResourceNotFoundException=404, InternalException=400. Nunca null para sinalizar erro.
---

# 2. Tratamento de erros

## Decisão

O tratamento de erro é centralizado em `infrastructure/handler/GlobalExceptionHandler`, um
`@RestControllerAdvice` que intercepta toda exceção não tratada que sobe dos controllers.

O service **lança** a exceção. Quem decide o status HTTP é o handler. Isso mantém a regra de
negócio sem nada de HTTP dentro dela.

## Exceções de negócio

Existem em `infrastructure/exception/`. São lançadas no service ou na entidade.

| Exceção | HTTP | Quando |
| --- | --- | --- |
| `ResourceNotFoundException` | 404 | Registro pedido não existe |
| `InternalException` | 400 | Regra de negócio violada |
| `IllegalStateException` | 409 | Estado inconsistente (ex: e-mail já confirmado) |
| `IllegalArgumentException` | 400 | Argumento inválido vindo de dentro do sistema |
| `BadCredentialsException` | 401 | Login ou senha errados |
| `AccessDeniedException` | 403 | Autenticado, mas sem permissão |

`IllegalArgumentException` e `IllegalStateException` já são mapeadas. Para "não encontrado", use
`ResourceNotFoundException`; `Optional.orElseThrow(() -> new ResourceNotFoundException(...))` é o
padrão do projeto.

## O que o handler já cobre

São as exceções que **não** precisam ser tratadas de novo. Escolha a mais específica.

| Exceção | HTTP | Quem lança |
| --- | --- | --- |
| `ResourceNotFoundException` | 404 | Você, no service |
| `InternalException` | 400 | Você, quando a regra de negócio nega |
| `IllegalStateException` | 409 | Você, para estado inconsistente |
| `IllegalArgumentException` | 400 | Qualquer camada; é o erro de código, não de dado |
| `BadCredentialsException` | 401 | Spring Security, no login |
| `AccessDeniedException` | 403 | Spring Security |
| `EntityNotFoundException` | 404 | JPA/Hibernate. Use `ResourceNotFoundException` no seu código |
| `DataIntegrityViolationException` | 409 | Banco: chave duplicada, violação de FK |
| `MethodArgumentNotValidException` | 400 | Spring, para `@Valid` que falhou; preenche `errors` |
| `Exception` | 500 | Rede de segurança; mensagem genérica, erro real no log |

## Formato da resposta

O DTO é o `ErrorResponse`, com três campos:

```json
{
  "code": "Not Found",
  "message": "Pessoa não encontrada com ID: 123",
  "errors": [{ "field": "email", "message": "Email inválido" }]
}
```

- `code` é o *reason phrase* do HTTP, como `Not Found` ou `Conflict`.
- `message` é a mensagem da exceção, em português.
- `errors` só aparece em erro de validação de entrada, e é montado a partir do
  `MethodArgumentNotValidException`.

O `ErrorResponse` é classe mutável com getters e setters, e não `record` como os outros DTOs. O
`GlobalExceptionHandler` monta a lista de erros em duas etapas, e o Jackson precisa poder mutar o
objeto. Ele também é a única resposta que pode ser devolvida em erro, então não segue a convenção
de DTO de entidade.

Não usamos [RFC 7807](https://www.rfc-editor.org/rfc/rfc7807) (Problem Details). Adotá-lo é escopo
novo, não uma pendência em aberto.

## O que NÃO fazer

- **Não devolva `null` para sinalizar erro.** Lance exceção. O `null` passa pelo service, quebra
  no controller, e o cliente recebe 200 com corpo vazio.
- **Não engula exceção em `try-catch`.** Se capturar, ou trata a recuperação, ou relança.
- **Não exponha stack trace.** O `handleGenericException` recebe `Exception` e devolve mensagem
  genérica; o erro real fica no log.
- **Não coloque try-catch no controller.** Ele não tem nada a recuperar.

## Exemplo

Errado:

```java
public NaturalPerson handle(FindNaturalPersonByIdQuery query) {
    try {
        return repository.findById(query.id()).get();
    } catch (NoSuchElementException e) {
        return null;  // o controller vai seguir com null
    }
}
```

Certo:

```java
public NaturalPerson handle(FindNaturalPersonByIdQuery query) {
    return repository.findByIdOrThrow(query.id());
}
```

`findByIdOrThrow` é um método `default` do próprio repository, e o handler devolve 404
automaticamente.

## Nota sobre 422

Não existe 422 neste projeto. Violação de regra de negócio vira 400 (`InternalException`) ou 409
(`IllegalStateException`). Se surgir a necessidade de 422, é decisão nova e vale um ADR.
