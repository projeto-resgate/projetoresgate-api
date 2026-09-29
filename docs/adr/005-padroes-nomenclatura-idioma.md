---
name: nomenclatura-e-idioma
status: aceito
summary: Código em inglês, texto em português, commits conventional em inglês. Listagem=FindAll, uma=Find*ById. Sempre Command/Response, nunca Request.
---

# 5. Nomenclatura e idioma

## Decisão

**Código em inglês. Texto em português.**

## Código: inglês

Classes, métodos, variáveis, pacotes e constantes.

```java
public class FamilyGroupController {
    public ResponseEntity<FamilyGroupResponse> findById(@PathVariable UUID id) { }
}
```

Não: `GrupoFamiliarController`, `buscarPorId`, `nomeGrupo`.

Constantes em `UPPER_SNAKE_CASE`. Pacotes em minúsculas, sem separador.

## Texto: português

| Onde | Exemplo |
| --- | --- |
| JavaDoc | `/** Busca um grupo familiar pelo seu ID. */` |
| Comentário | `// O friendlyId é gerado pelo banco a partir da sequence` |
| Mensagem de exceção | `"Pessoa não encontrada com ID: " + id` |
| `@DisplayName` de teste | `"Deve listar grupos familiares com paginação"` |
| Nome de migration | `V012__add_phone_to_natural_person.sql` |

Comentário explica **por que**, não **o que**. A linha já diz o que faz; o comentário existe para o
motivo que não está no código.

## Sufixos

| Tipo | Sufixo | Exemplo |
| --- | --- | --- |
| Controller | `Controller` | `FamilyGroupController` |
| Service | `Service` | `FindAllFamilyGroupsService` |
| Repository | `Repository` | `FamilyGroupRepository` |
| Caso de uso | `UseCase` | `CreateFamilyGroupUseCase` |
| Objeto de caso de uso | `Command` / `Query` | `CreateFamilyGroupCommand` |
| DTO de entrada (API) | `Command` | `CreateFamilyGroupCommand` |
| DTO de saída (API) | `Response` | `FamilyGroupResponse` |
| Exceção | `Exception` | `ResourceNotFoundException` |

## Verbo do caso de uso

O nome começa com o que a operação **faz**, e isso é o mesmo no `UseCase`, no `Query`, no `Service`
e no método do controller.

| Operação | Verbo | Tipo |
| --- | --- | --- |
| Listar, com ou sem filtro | `FindAll` | `FindAllFamilyGroupsUseCase` |
| Buscar um por id | `Find*ById` | `FindFamilyGroupByIdUseCase` |
| Busca digitada, top-N | `Autocomplete` | `AutocompleteNaturalPersonUseCase` |
| Criar | `Create` | `CreateFamilyGroupUseCase` |
| Alterar | `Update` | `UpdateFamilyGroupUseCase` |
| Remover (soft delete) | `SoftDelete` | `SoftDeleteFamilyGroupUseCase` |
| Associar | `Add*` | `AddNaturalPersonToFamilyGroupUseCase` |
| Desassociar | `Remove*` | `RemoveNaturalPersonFromFamilyGroupUseCase` |

`Search*` não é verbo de caso de uso neste projeto. Listagem é `findAll`, com ou sem filtro: o
filtro é detalhe do endpoint, não o que ele é.

**Não existe `*Request`.** A entrada da API é `*Command`. Ver
[ADR 008](008-formato-dtos-api.md).

## Nomes descritivos

O nome revela a intenção, sem prefixo de tipo.

| Ruim | Bom |
| --- | --- |
| `u`, `usr`, `d` | `user`, `currentUser`, `daysSinceLastLogin` |
| `strName`, `iCount` | `name`, `count` |
| `Manager`, `Processor`, `Helper` | `FamilyGroupService`, `CnpjValidator` |

## Commit

Conventional Commits, **em inglês**, no formato `tipo(escopo): descrição`.

```
feat(familygroup): adiciona vínculo de pessoa ao grupo
fix(auth): corrige expiração do refresh token
docs(adr): registra decisão sobre Testcontainers
```

Escopos usados no projeto: `familygroup`, `naturalperson`, `legalperson`, `program`, `user`,
`auth`, `swagger`, `adr`.
