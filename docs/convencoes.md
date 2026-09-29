# Convenções

Padrões práticos com código real do projeto. Para o motivo por trás de cada padrão, veja os
[ADRs](adr/README.md). Este arquivo é o que você consulta para escrever código que parece com o
resto.

## Nomenclatura

| Tipo          | Padrão                    | Exemplo                                     |
|---------------|---------------------------|---------------------------------------------|
| Classe        | PascalCase com sufixo     | `CreateFamilyGroupService`                  |
| Método        | camelCase                 | `handle`, `findByIdOrThrow`                 |
| Variável      | camelCase                 | `familyGroup`, `naturalPersons`             |
| Constante     | `UPPER_SNAKE_CASE`        | `MAX_LOGIN_ATTEMPTS`                        |
| Pacote        | minúsculas, sem separador | `naturalperson`, `familygroup`              |
| Tabela/coluna | `snake_case`              | `family_group_natural_person`, `birth_date` |
| Índice        | `idx_{tabela}_{coluna}`   | `idx_natural_person_cpf`                    |

**Código em inglês, documentação em português.** Isso vale para classes, métodos, variáveis e
comentários. Ver [ADR 005](adr/005-padroes-nomenclatura-idioma.md).

Os pacotes de feature são **uma palavra**: `familygroup`, não `family-group` nem `family_group`.

## Entidade de domínio

A regra de negócio mora na entidade. Ela não é anêmica e não usa Lombok.

```java

@Entity
public class FamilyGroup extends AuditableEntity {

    @Id
    private UUID id;
    // ... campos privados, sem setter público

    protected FamilyGroup() {
        // exigido pelo JPA
    }

    private FamilyGroup(String friendlyId, String name, /* ... */) {
        // construtor privado
    }

    // Fábrica: o único jeito de criar um válido
    public static FamilyGroup create(String friendlyId, String name, /* ... */) {
        // validações aqui, não no service
    }

    // Updater: alteração parcial sem setter exposto
    public Updater update() {
        return new Updater();
    }

    public static class Updater {
        public Updater name(String name) { /* ... */
            return this;
        }

        public FamilyGroup apply() { /* valida e aplica */
            return familyGroup;
        }
    }
}
```

O padrão `update()...apply()` existe para deixar explícito que a alteração só vale depois de
`apply()`, e para permitir validações que dependem do estado anterior.

Soft delete é **automático** quando a entidade tem a anotação:

```java

@SQLRestriction("deleted_at IS NULL")
public class NaturalPerson extends AuditableEntity { /* ... */
}
```

Com isso, `findById` já ignora deletados. **Não** filtre `deletedAt` à mão em query: é redundante
e o dia que alguém esquecer, vaza dado deletado. Note que nem toda entidade tem a anotação —
`User` e `FamilyGroup` não têm soft delete.

## Repository

Métodos de leitura derivados eJPQL só quando necessário.

```java
public interface NaturalPersonRepository extends JpaRepository<NaturalPerson, UUID> {
    Optional<NaturalPerson> findByEmailIgnoreCase(String email);

    // findByIdOrThrow: o Optional vira exceção, no próprio repository
    default NaturalPerson findByIdOrThrow(UUID id) {
        return findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Pessoa não encontrada com ID: " + id));
    }
}
```

O `findByIdOrThrow` como método `default` centraliza a mensagem de 404 e evita repetir o
`orElseThrow` em todo service. O projeto prefere assim a um `ResourceNotFoundException` lançado
manual no service.

Para filtro dinâmico, use o `SpecificationBuilder`:

```java
Specification<FamilyGroup> filters = new SpecificationBuilder<FamilyGroup>()
        .with("name", "~", query.name())
        .build();

Page<FamilyGroup> page = repository.findAll(filters, query.pageable());
```

A operação vem do que o filtro precisa casar:

| Operação          | Faz                             | Use para                          |
|-------------------|---------------------------------|-----------------------------------|
| `:`               | igualdade exata, case-sensitive | enum, UUID, CPF, CNPJ, status     |
| `~`               | ILIKE: contém, ignorando caixa  | nome, razão social, busca parcial |
| `>` `<` `>=` `<=` | comparação                      | intervalo de data ou número       |

`~` **só funciona em propriedade de texto**; em coluna numérica, de data ou enum a query falha com
mensagem sugerindo `:`. A operação vem do que o filtro precisa casar, não do tipo da coluna.

`null` ou string vazia no valor **desliga** o filtro, o que torna o filtro opcional de graça.

## Caso de uso e service

A interface fica em `usecase/`, a implementação em `service/`. Nunca em `usecase/impl/`.

```java
public interface CreateFamilyGroupUseCase {
    FamilyGroup handle(CreateFamilyGroupCommand command);
}

@Service
public class CreateFamilyGroupService implements CreateFamilyGroupUseCase {

    private final FamilyGroupRepository repository;

    public CreateFamilyGroupService(FamilyGroupRepository repository) {
        this.repository = repository;
    }

    @Override
    @Transactional
    public FamilyGroup handle(CreateFamilyGroupCommand command) {
        FamilyGroup familyGroup = FamilyGroup.create(/* ... */);
        return repository.save(familyGroup);
    }
}
```

O método se chama **`handle`**, não `execute`. A transação fica no service, não no controller.

### Command e Query

Leitura e escrita são separadas em tipos diferentes, seguindo CQRS leve.

```java
// usecase/command/ — intenção de escrever
public record CreateFamilyGroupCommand(
                String name,
                AddressCommand address
        ) {
    public CreateFamilyGroupCommand withId(UUID id) {
        return new CreateFamilyGroupCommand(name, address); // devolve nova instância
    }
}
```

Command e Query são `record` imutáveis, e o método `withX(...)` devolve uma **nova instância** em
vez de mutar. Para consulta, o padrão é receber o `Pageable` pronto quando a paginação é do
cliente:

```java
public record FindAllFamilyGroupsQuery(String name, Pageable pageable) {
}
```

## DTOs da API

**Não existe `*Request`.** A entrada da API é `*Command` e a saída é `*Response`. Ver
[ADR 008](adr/008-formato-dtos-api.md).

```java
// api/dto/ — saída
public record FamilyGroupSummaryResponse(
                UUID id,
                String friendlyId,
                String name,
                Long registeredPeopleCount
        ) {
    public static FamilyGroupSummaryResponse fromEntity(FamilyGroup entity, Long count) {
        return new FamilyGroupSummaryResponse(
                entity.getId(),
                entity.getFriendlyId(),
                entity.getName(),
                count
        );
    }
}
```

O `fromEntity` é a única forma de construir um Response. Ele fica no próprio DTO, não em um mapper
separado. O retorno de `Page<XResponse>` é montado com `PageImpl`, preservando `pageable` e
`totalElements`:

```java
return new PageImpl<>(

toSummaries(page.getContent()),page.

getPageable(),page.

getTotalElements());
```

## Controller

Sem anotações de escrita no controller, sem regra de negócio, sem acesso a repository.

```java

@RestController
@RequestMapping("/family-group")
@Tag(name = "Family Group", description = "Endpoints para gerenciamento de Grupos Familiares")
public class FamilyGroupController {

    private final CreateFamilyGroupUseCase createUseCase;

    @PostMapping
    @Operation(summary = "Criar Grupo Familiar", description = "...")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Criado com sucesso", content = @Content),
            @ApiResponse(responseCode = "400", description = "Dados inválidos", content = @Content)
    })
    public ResponseEntity<FamilyGroupResponse> create(@RequestBody @Valid CreateFamilyGroupCommand command) {
        FamilyGroup familyGroup = createUseCase.handle(command);
        return ResponseEntity.created(uriBuilder("/family-group/", familyGroup.getId()))
                .body(FamilyGroupResponse.fromEntity(familyGroup));
    }
}
```

### Paginação

Os endpoints recebem `page` e `size` explícitos e montam o `Pageable` à mão, com `Sort` fixo.
Não há injeção automática de `Pageable`.

```java
public ResponseEntity<Page<FamilyGroupSummaryResponse>> findAll(
        @Parameter(description = "Nome") @RequestParam(required = false) String name,
        @Parameter(description = "Número da página") @RequestParam(defaultValue = "0") int page,
        @Parameter(description = "Tamanho da página") @RequestParam(defaultValue = "10") int size
) {
    Pageable pageable = PageRequest.of(page, size, Sort.by("name").ascending());
    return ResponseEntity.ok(findAllUseCase.handle(new FindAllFamilyGroupsQuery(name, pageable)));
}
```

A resposta é a página do Spring serializada, com os dados em `$.content` e o total em
`$.totalElements`.

### Roteiro de um endpoint novo

1. Crie o `record` do command em `usecase/command/`, com `withX(...)` para o id da rota.
2. Crie o DTO de entrada em `api/dto/` ou reutilize o command direto no controller. O projeto usa
   o command direto no corpo quando o formato bate.
3. Crie a interface `XUseCase` em `usecase/`, com `handle`.
4. Crie o `XService` em `service/`, com a regra e a transação.
5. Crie o `XResponse` em `api/dto/` como `record` com `fromEntity`.
6. Crie o método no controller com `@Operation` e `@ApiResponses` completos.

## Erros

Exceção específica de negócio, nunca `null` como sinal de erro.

| Exceção                           | HTTP | Quando usar                                     |
|-----------------------------------|------|-------------------------------------------------|
| `ResourceNotFoundException`       | 404  | Recurso não existe.                             |
| `InternalException`               | 400  | Regra de negócio violada.                       |
| `IllegalArgumentException`        | 400  | Argumento inválido (validação).                 |
| `IllegalStateException`           | 409  | Estado inconsistente (ex: vincular duas vezes). |
| `DataIntegrityViolationException` | 409  | Violação de constraint no banco.                |

O mapeamento está em `infrastructure/handler/GlobalExceptionHandler.java`. A resposta é sempre
`ErrorResponse`, com o formato `{code, message, errors?}` — **não** é RFC 7807. O `code` é o
reason phrase do HTTP, como `Not Found`.

Detalhe importante: exceção do tipo genérico `RuntimeException` não é mapeada e vira 500 com
mensagem genérica. Prefira sempre as exceções de negócio.

## Validação

Anotações em `shared/validation/annotation/`, implementadas em `shared/validation/validator/`.

```java

@NotBlank(message = "Nome é obrigatório")
@Size(max = 150, message = "Nome deve ter no máximo 150 caracteres")
private String name;

@RG(message = "RG inválido")
private String rg;

@Cellphone(message = "Celular inválido")
private String cellphone;
```

CPF e email usam o bean validation padrão (`@Email`). No controller, sempre `@Valid` no
`@RequestBody`, e a anotação de restrição no record do command.

### Verbo do método do controller

O nome do método segue o que o endpoint faz, e é o mesmo nome do caso de uso:

| Endpoint                    | Verbo          | Caso de uso            |
|-----------------------------|----------------|------------------------|
| `POST /recurso`             | `create`       | `Create*UseCase`       |
| `PUT /recurso/{id}`         | `update`       | `Update*UseCase`       |
| `DELETE /recurso/{id}`      | `delete`       | `SoftDelete*UseCase`   |
| `GET /recurso`              | `findAll`      | `FindAll*UseCase`      |
| `GET /recurso/{id}`         | `findById`     | `Find*ByIdUseCase`     |
| `GET /recurso/autocomplete` | `autocomplete` | `Autocomplete*UseCase` |

`search` não é verbo de listagem neste projeto. Listagem é `findAll`, com ou sem filtro.

## Transações

`@Transactional` fica no service. Leituras usam `@Transactional(readOnly = true)`.

```java

@Override
@Transactional(readOnly = true)
public Page<FamilyGroupSummaryResponse> handle(FindAllFamilyGroupsQuery query) { /* ... */ }
```

## Relação muitos para muitos sem entidade de junção

Quando o vínculo entre dois agregados não tem atributos próprios e não precisa ser consultado
isolado, o padrão do projeto é **não** tratar o vínculo como entidade. Em vez de carregar o
agregado, modificar a coleção e salvar, insere-se direto na tabela de junção com query nativa.

```java

@Modifying
@Query(value = """
        insert into family_group_natural_person (family_group_id, natural_person_id)
        values (:familyGroupId, :naturalPersonId)
        on conflict do nothing
        """, nativeQuery = true)
int linkNaturalPerson(@Param("familyGroupId") UUID familyGroupId,
                      @Param("naturalPersonId") UUID naturalPersonId);
```

Três motivos para o insert direto:

1. **Evita lost update.** Carregar o agregado, adicionar na lista e salvar reescreve a coleção
   inteira; duas requisições simultâneas podem se sobrescrever.
2. **`on conflict do nothing` é atômico.** A transação perdedora recebe `0` de retorno, em vez de
   violar a PK.
3. **O retorno `0` carrega o significado.** O service transforma em `IllegalStateException` e o
   cliente recebe 409.

O mesmo vale para desvincular, com um `delete` nativo.

Para **ler** um vínculo paginado, o padrão é um JPQL com `join` interno, e não carregar a
coleução:

```java

@Query("""
        select np from FamilyGroup fg
        join fg.naturalPersonList np
        where fg.id = :familyGroupId
        """)
Page<NaturalPerson> findNaturalPersonsByFamilyGroupId(@Param("familyGroupId") UUID familyGroupId,
                                                      Pageable pageable);
```

O `join` interno é obrigatório: com `left join`, um grupo sem pessoas devolveria uma linha com
`null` e a contagem da paginação ficaria errada.

## Comentários e JavaDoc

Em português, e explicando **por quê**, não **o que** a linha faz.

```java
// ruim: repete o código
// Define o nome do grupo
String name = group.getName();

// bom: explica a decisão
// Valida o grupo antes de criar a pessoa: a confirmação por e-mail não é
// transacional, então o grupo precisa existir antes do envio.
repository.

findByIdOrThrow(query.familyGroupId());
```

Nada de comentário explicando a assinatura de um método que já se lê sozinho.
