# ADRs — Architecture Decision Records

Cada ADR registra **uma decisão**, o contexto que a motivou e as consequências. Serve para responder
"por que o projeto é assim" depois que alguém se pergunta "por que diabos fizemos isso".

## Como ler

- O projeto **ainda não foi para produção**, então um ADR aceito pode ser corrigido diretamente.
  Reescreva o texto da decisão; não deixe um aviso apontando para outro ADR.
- Se o código divergir do ADR, **o código está errado ou o ADR está desatualizado**. Não siga o
  ADR cegamente: leia o código e corrija o ADR. Um ADR errado é pior que nenhum, porque faz
  alguém (ou alguma IA) escrever errado com confiança.
- Quando a decisão mudar de verdade, **reescreva o ADR**. O projeto não foi para produção e não há
  histórico a preservar; o `git log` já guarda o que mudou.
- ADRs registram decisões, não manuais de uso. Para "como escrever um controller", veja
  [`convencoes.md`](../convencoes.md).

## Formato

```markdown
# N. Título da decisão
```

Seções: `## Decisão` (obrigatória) e, quando ajudam a leitura, `## Contexto`, `## Consequências`,
`## O que NÃO fazer` e `## Como manter`.

Comece pela regra. O leitor quer saber **o que vale hoje**, não a jornada até chegar lá. Contexto
entra só quando sem ele a decisão fica incompreensível, e nesse caso é curto.

Formato curto é a norma, não a exceção: ADR de 20 linhas que se segue é melhor que ADR de 200
linhas que ninguém lê.

## Índice semântico

Uma linha por decisão. Leia só a linha que corresponde à sua tarefa; abra o arquivo só se a linha
não bastar.

| ADR | Quando ler | O que manda |
| --- | --- | --- |
| [001](001-padroes-arquiteturais.md) | Criar pasta, mover classe, decidir dependência | Pasta por feature, não por camada. `domain` não importa nada de fora; `service` nunca fica em `usecase/impl/`. |
| [002](002-tratamento-de-erros.md) | Lançar exceção, mapear status HTTP, escrever `ErrorResponse` | Sobe exceção, o `GlobalExceptionHandler` decide o status. `ResourceNotFoundException`=404, `InternalException`=400. Nunca `null` para sinalizar erro. |
| [003](003-gerenciamento-banco-dados.md) | Criar tabela, coluna, índice, enum, corrigir SQL | Flyway é o único dono do schema e `ddl-auto=validate`. **Nunca edite migration commitada** — crie a próxima. |
| [004](004-estrategia-testes.md) | Escolher tipo de teste, decorar classe de teste | Unitário=`MockitoExtension`, controller=`@WebMvcTest`, banco=`@DataJpaTest` + `PostgresIntegrationTest`. Nunca `@SpringBootTest` nem H2. |
| [005](005-padroes-nomenclatura-idioma.md) | Dar nome a classe, método, variável ou commit | Código em inglês, texto em português, commit conventional em inglês. Listagem=`FindAll`, uma=`Find*ById`. Entrada é `*Command`, nunca `*Request`. |
| [006](006-seguranca-autenticacao.md) | Mexer em rota pública, token, login, permissão | JWT Auth0 stateless via `Authorization: Bearer`. **Autorização por role ainda não existe** — nenhum `@PreAuthorize` no código. |
| [007](007-testcontainers-postgres.md) | Escrever teste que toca banco, errar com H2, errar com `ddl-auto` | Todo teste de banco estende `PostgresIntegrationTest`, Flyway real, H2 proibido. |
| [008](008-formato-dtos-api.md) | Criar DTO de entrada ou saída, decidir `record` vs classe | Entrada=`*Command`, saída=`*Response`, ambos `record` com `fromEntity`. Zero classes `*Request` no projeto. |
| [009](009-busca-com-specification-builder.md) | Escrever filtro dinâmico, escolher operação do `with()` | Igualdade `:`, ILIKE `~` (só texto), comparação `> < >= <=`. `build()` devolve `null` sem filtro. `OR`/faixa exige `@Query`. |

## Onde a decisão existe mas o código ainda não

Uma coisa que este índice deixa explícita: alguns ADRs registram decisão de arquitetura que ainda
não virou código. Isso é diferente de um ADR "superado" — a decisão continua válida, só falta
implementar.

| Tema | Onde | Estado |
| --- | --- | --- |
| Autorização por role | [ADR 006](006-seguranca-autenticacao.md), seção 2 | Roles existem no `User`, mas nada consulta para autorizar |

A discussão de como implementar está em
[`RFC 0001`](../rfc/0001-autorizacao-por-role.md). Trate autorização como escopo novo: não escreva
código que dependa dela sem antes decidir.
