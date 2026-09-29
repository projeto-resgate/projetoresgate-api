---
name: formato-dtos-api
status: aceito
summary: Entrada=*Command, saída=*Response, ambos record com fromEntity. Nenhuma classe *Request existe no projeto.
---

# 8. Formato dos DTOs de API: Command e Response

## Decisão

Entrada da API é `*Command`. Saída é `*Response`. Leitura é `*Query`. Todos são `record`
imutável. **Nunca** `*Request` — não existe nenhuma classe com esse sufixo no projeto.

O nome `Command` carrega o significado certo: não é só o corpo da requisição, é uma **intenção
de mudança de estado**. `Request` sugere apenas transporte, o que fica errado quando o mesmo
objeto é repassado ao caso de uso.

Em alguns casos de uso, entrada e saída são o **mesmo** tipo, e isso é intencional: não vale
forçar dois tipos onde um basta.

## Onde fica

| Papel | Sufixo | Onde |
| --- | --- | --- |
| Entrada da API | `*Command` | `api/dto/` |
| Saída da API | `*Response` | `api/dto/` |
| Objeto de caso de uso (escrita) | `*Command` | `usecase/command/` |
| Objeto de caso de uso (leitura) | `*Query` | `usecase/query/` |

## DTO de resposta

Todo `*Response` de entidade é um `record` com um único ponto de construção: o `fromEntity`
estático, definido no próprio DTO.

```java
public record FamilyGroupSummaryResponse(
        UUID id,
        String friendlyId,
        String name,
        Long registeredPeopleCount
) {
    public static FamilyGroupSummaryResponse fromEntity(FamilyGroup entity, Long count) {
        return new FamilyGroupSummaryResponse(
                entity.getId(), entity.getFriendlyId(), entity.getName(), count
        );
    }
}
```

Três regras:

1. **Sem Lombok nos Responses.** `record` já cobre o papel, e `@Data` em DTO de saída é
   desnecessário.
2. **O `fromEntity` é o único caminho de construção.** Isso torna explícito que o DTO é um
   contrato de leitura, e impede que um `new` espalhado no service produza campo errado.
3. **Sem mapper separado.** Para DTOs simples, um mapper por entidade é mais classe do que
   necessário. Só considere um se houver transformação de verdade.

## DTO de listagem: o que entra

Um DTO de listagem devolve **só o que a tela precisa**. Não é a entidade inteira com campo a
mais: cada endpoint tem seu próprio `*Response`, mesmo quando representa a mesma entidade.

```java
// tela de listagem de grupos
public record FamilyGroupSummaryResponse(UUID id, String friendlyId, String name, Long registeredPeopleCount)

// grid de pessoas do grupo
public record FamilyGroupNaturalPersonResponse(UUID id, String name, String rg, String cpf, String cellphone)
```

Isso é proteção de dados tanto quanto de banda. O campo que não está no record não serializa, e
não há como vazar por engano.

## Exceção: `ErrorResponse`

O DTO de erro é classe mutável com getters e setters, não record. Não é inconsistência a corrigir:
o `GlobalExceptionHandler` monta a lista de erros de validação em duas etapas, e o Jackson precisa
mutar o objeto. Ele também é a **única** resposta que pode aparecer em erro, então não segue a
convenção de entidade. Ver [ADR 002](002-tratamento-de-erros.md).

## O que NÃO fazer

- **Não criar `*Request`.** A entrada é `*Command`.
- **Não usar Lombok em `*Response`.** É `record` com `fromEntity`.
- **Não devolver a entidade inteira** como resposta de listagem. Crie o `*Response` enxuto.
- **Não criar um mapper por entidade** só por simetria com a entidade.

## Impacto em teste

Quando um campo sai de um `*Response`, o teste de controller precisa ganhar um
`jsonPath(...).doesNotExist()`. Um DTO que encolheu é uma melhoria de segurança, e o teste é o que
comprova.
