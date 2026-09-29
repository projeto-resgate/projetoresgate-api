---
name: specification-builder
status: aceito
summary:
  Filtros dinâmicos via SpecificationBuilder: igualdade com :, ILIKE com ~ (só texto), comparações > < >= <=. build() devolve null sem filtro.
---

# 9. Busca dinâmica com SpecificationBuilder

## Decisão

Toda listagem filtrável monta o filtro com `SpecificationBuilder`, uma linha por filtro:

```java
Specification<FamilyGroup> filters = new SpecificationBuilder<FamilyGroup>()
        .with("name", "~", query.name())
        .build();

Page<FamilyGroup> page = repository.findAll(filters, query.pageable());
```

O repository implementa `JpaSpecificationExecutor<T>`.

## As operações

A operação vem do **que o filtro precisa casar**, não do tipo da coluna.

| Operação          | Faz                             | Use para                          |
|-------------------|---------------------------------|-----------------------------------|
| `:`               | igualdade exata, case-sensitive | enum, UUID, CPF, CNPJ, status     |
| `~`               | ILIKE: contém, ignorando caixa  | nome, razão social, busca parcial |
| `>` `<` `>=` `<=` | comparação                      | intervalo de data ou número       |

`:` e `~` são operações diferentes de propósito: pedir igualdade em `String` não pode custar a
busca parcial, que é o que o usuário faz ao digitar num campo de nome.

`~` se chama **ILIKE** por ser o mesmo operador do Postgres — match que ignora caixa. A implementação
é `lower(coluna) LIKE lower(padrao)` com escape explícito, que é equivalente ao `ILIKE` e ainda
escapa `%` e `_` de graça.

`:` funciona em qualquer tipo, porque é igualdade. `~` **só funciona em propriedade de texto** e
lança exceção em coluna numérica, de data ou enum: o Postgres não tem `numeric ~~ unknown`, ou seja,
`LIKE` não existe fora de texto. A mensagem aponta para `:`.

`>` e `<` são estritos; `>=` e `<=` incluem o próprio valor.

## Filtro opcional sai de graça

`null` e string vazia **desligam** o filtro, então parâmetro opcional não precisa de `if`:

```java
new SpecificationBuilder<FamilyGroup>()
        .

with("name","~",query.name())      // null: sem filtro
        .

build();
```

Com todos os filtros desligados, `build()` devolve `null`, e `findAll(null, pageable)` funciona.

## Propriedade aninhada usa ponto

```java
.with("program.id",":",programId)
.

with("address.city","~",city)
```

## Combinação

O `with()` sempre combina com `AND`. Para `OR`, agregação ou intervalo, escreva `@Query` com JPQL
explícito — é mais legível que forçar o builder.

## Validação

| Entrada inválida                    | Quando falha                                             |
|-------------------------------------|----------------------------------------------------------|
| Operação desconhecida (`=`, `LIKE`) | no `with()`, não na query                                |
| `~` em propriedade não textual      | na execução da query, com mensagem dizendo para usar `:` |

Falhar no `with()` é intencional: erro de digitação na operação aparece na hora de escrever a
linha, não em produção depois do deploy.

## Valores com `%` e `_`

`~` escapa `%`, `_` e `\`, então o valor é sempre literal. O usuário digitando `100%` não vira
curinga. Para curinga de verdade, use `@Query`.

## Limite conhecido

A chave de propriedade é `String` sem verificação em tempo de compilação. Propriedade digitada
errada só falha quando a query roda. Confira o nome na entidade antes de escrever.

## Cobertura atual

14 filtros em 6 services. Texto livre está em `~`; identificador e enum, em `:`. O contrato
inteiro está em `SpecificationBuilderIntegrationTest`.

## O que NÃO fazer

- **Não** escrever JPQL com condicional para variar filtros.
- **Não** escolher a operação pelo tipo da coluna. Escolha pelo que o filtro precisa casar.
- **Não** filtrar CPF, CNPJ ou status com `~`: vira busca parcial e traz registro que não
  corresponde ao que o usuário pediu.
- **Não** filtrar soft delete à mão. `@SQLRestriction` já filtra.
- **Não** usar `~` esperando curinga do usuário.
- **Não** passar `""` esperando filtrar por vazio. String vazia desliga o filtro.
