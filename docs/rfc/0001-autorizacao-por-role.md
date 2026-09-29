# 0001. Autorização por role

Data: 2026-09-29
Status: Proposta
Autor: Documentação inicial do projeto

## Problema

O [ADR 006](../adr/006-seguranca-autenticacao.md) descreve autorização baseada em roles com
`@PreAuthorize("hasRole('ADMIN')")`. **Esse código não existe.** Não há um único `@PreAuthorize`
em todo o projeto.

Na prática, a autorização está assim:

```java
// infrastructure/security/SecurityConfigurations.java
.authorizeHttpRequests(auth ->auth
        .

requestMatchers(HttpMethod.POST, "/user/login").

permitAll()
// ... outras rotas públicas
        .

anyRequest().

authenticated()
)
```

Ou seja: **qualquer usuário autenticado acessa qualquer endpoint.** Não existe separação entre
usuário comum, voluntário e administrador. O enum `UserRole` existe e o `User` recebe `ADMIN` na
criação, mas o papel não é consultado em lugar nenhum para autorização.

O risco é concreto: `GET /family-group/{id}` devolve dados de endereço e renda familiar. Um
usuário autenticado normal, indicado por alguém, consegue ler o cadastro de qualquer outra
família, se souber o UUID.

## Contexto

O que já existe:

- `UserRole` com `ADMIN` e `VOLUNTEER` (`core/identity/user/domain/enums/UserRole.java`);
- o `User` já popula `roles` na criação;
- `SecurityUtils` como helper de acesso ao usuário autenticado;
- a infraestrutura de Spring Security completa, com filtro JWT e CORS.

O que não existe:

- nenhuma anotação `@PreAuthorize` ou `@Secured`;
- nenhum `hasRole` ou `hasAuthority` na configuração;
- nenhum teste de autorização por papel;
- `AccessDeniedException` está mapeada para 403 no `GlobalExceptionHandler`, mas nada a lança
  via autorização de método.

## Opções

### Opção A — `@PreAuthorize` em cada método

Anotar os métodos do controller:

```java

@GetMapping("/{id}")
@PreAuthorize("hasAnyRole('ADMIN', 'VOLUNTEER')")
public ResponseEntity<FamilyGroupResponse> findById(@PathVariable UUID id) { ...}
```

**Prós**

- Declarativo e legível no ponto de uso; a política fica visível junto da operação.
- É exatamente o que o ADR 006 já descreve, então a documentação volta a bater com o código.
- Testável com `@WithMockUser(roles = "ADMIN")` em `@WebMvcTest`.

**Contras**

- Precisa de `@EnableMethodSecurity` na configuração, hoje ausente.
- A política se espalha por todos os controllers: 5 controllers, dezenas de métodos. Fácil
  esquecer um método novo, e esquecer significa **permitir**, que é o modo de falha perigoso.
- Difícil responder "quem pode acessar o que?" sem varrer o código.

### Opção B — Regras centralizadas por URL

Configurar tudo no `SecurityConfigurations`, com `requestMatchers` por padrão HTTP:

```java
.requestMatchers(HttpMethod.GET, "/family-group/**").

hasAnyRole("ADMIN","VOLUNTEER")
.

requestMatchers(HttpMethod.POST, "/family-group/**").

hasRole("ADMIN")
```

**Prós**

- Um único lugar com toda a política, respondendo "quem acessa o quê" de imediato.
- O padrão "negar por padrão" fica explícito: a última regra continua `authenticated()`, e dá
  para trocar por `.denyAll()` só para testar se a política está completa.

**Contras**

- URL não descreve ação. `GET /family-group/{id}` e `GET /family-group` teriam a mesma regra,
  o que é raro na prática.
- Mistura a política de autenticação com a de autorização no mesmo arquivo, que já está
  crescendo.
- Não cobre regra que dependa do dado da requisição, e não é visível na IDE ao ler o método.

### Opção C — Híbrido: padrão por URL, exceções por método

URL define o padrão do recurso; `@PreAuthorize` cobre os casos que a URL não expressa (ex: só o
dono pode editar o próprio registro).

**Prós**

- As regras de permissão gerais ficam visíveis num lugar, e as específicas ficam no método.
- A regra de permissão por dono é impossível de expressar só por URL.

**Contras**

- Dois lugares para procurar, o que reduz o ganho de legibilidade da opção B.
- Exige convenção sobre qual mecanismo usar, ou vira inconsistência.

## Recomendação

**Opção C — híbrido**, com `@EnableMethodSecurity` habilitado.

O critério é o **modo de falha** quando alguém erra. Num sistema que guarda renda e endereço de
família, o erro que não pode acontecer é liberar acesso indevido, então o padrão precisa negar.

- A opção A falha para o lado permissivo: um método novo sem `@PreAuthorize` é liberado, e nada
  avisa. Um teste parametrizado que exige anotação em todo método de controller mitiga, mas
  depende de alguém lembrar de manter o teste em dia.
- A opção B falha para o lado seguro: uma rota nova cai no `authenticated()` e é negada. O
  sintoma é um 403 para o usuário, não uma exposição de dado.
- A opção C herda a segurança da B e adiciona a expressividade do `@PreAuthorize` para o que a
  URL não consegue dizer, como "só o dono edita o próprio registro".

O custo é ter a política em dois lugares. Aceito, porque o que os dois lugares somam é mais fácil
de auditar do que a alternativa, que é anotar método por método torcendo para não esquecer nenhum.

Estrutura sugerida:

```java
// SecurityConfigurations: padrão por recurso
.requestMatchers(HttpMethod.GET, "/family-group/**").

hasAnyRole("ADMIN","VOLUNTEER")
.

requestMatchers(HttpMethod.POST, "/family-group/**").

hasRole("ADMIN")
.

anyRequest().

authenticated()

// no método, só quando a URL não basta
@GetMapping("/{id}")
@PreAuthorize("hasAnyRole('ADMIN', 'VOLUNTEER')")
public ResponseEntity<FamilyGroupResponse> findById(@PathVariable UUID id) { ...}
```

## Impacto

- **Código:** `@EnableMethodSecurity` em `SecurityConfigurations`; regras por recurso nas
  `requestMatchers`; `@PreAuthorize` nos métodos que a URL não expressa; atualizar a seção 2 do
  [ADR 006](../adr/006-seguranca-autenticacao.md).
- **Banco:** nenhuma migration. O `UserRole` já existe e o `User` já popula `roles`.
- **API:** rotas liberadas hoje mudam de comportamento. Endpoints de escrita passam a exigir
  `ADMIN`. Isso pode quebrar o frontend, que hoje depende do acesso amplo. **Coordenação com o
  time de front é obrigatória.**
- **Testes:** os 5 `@WebMvcTest` passam a exigir `@WithMockUser` com o papel certo. Ajustar
  `WithMockCustomUser` para aceitar `roles`. Teste novo garantindo que endpoint sem anotação
  falha.
- **Docs:** novo ADR, atualizar a seção 2 do [ADR 006](../adr/006-seguranca-autenticacao.md) e
  remover o item correspondente de "Coisas que este projeto ainda não faz" do
  [`AGENTS.md`](../../AGENTS.md).

## Decisão

Pendente. Enquanto não houver decisão registrada, trate a autorização por role como **inexistente**
ao planejar qualquer trabalho que dependa dela.
