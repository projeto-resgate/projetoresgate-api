---
name: seguranca-autenticacao
status: aceito
summary:
  JWT Auth0 stateless via Authorization Bearer. Autorização por role ainda não existe: nenhum @PreAuthorize no código.
---

# 6. Segurança e autenticação

## Decisão

Spring Security com **JWT** (Auth0) para autenticação. A API é stateless: nada de sessão no
servidor, e o cliente manda `Authorization: Bearer <token>` em toda requisição protegida.

### 1. Autenticação (Stateless)

A API é **stateless**, ou seja, não mantém sessão no servidor. O cliente deve enviar um token JWT válido no cabeçalho
`Authorization` de cada requisição protegida.

* **Fluxo:**
    1. Cliente envia credenciais (email/senha) para `/auth/login`.
    2. Servidor valida e retorna um JWT assinado.
    3. Cliente armazena o JWT (ex: LocalStorage) e o envia nas próximas requisições: `Authorization: Bearer <token>`.

### 2. Papéis e Autorização

O `User` tem papéis, guardados em `core/identity/user/domain/enums/UserRole.java`
(`ADMIN`, `VOLUNTEER`), e recebe `ADMIN` na criação.

**A autorização em si não está implementada.** Hoje a regra em `SecurityConfigurations` termina em
`anyRequest().authenticated()`, então qualquer usuário autenticado acessa qualquer endpoint
protegido. Os papéis existem e são gravados, mas não são consultados para autorizar.

Como implementar isso está discutido em
[RFC 0001: Autorização por role](../rfc/0001-autorizacao-por-role.md). Até lá, trate autorização
por papel como escopo novo.

### 3. Proteção de Endpoints

* **Públicos:** Login, renovação de token, recuperação de senha, confirmação de e-mail e `/actuator/health` são
  liberados explicitamente em `SecurityConfigurations`. `POST /user` só é público com `api.user-registration.public=true`
  (desenvolvimento). A documentação (Swagger/OpenAPI) só é pública quando `springdoc.swagger-ui.enabled=true` (dev);
  em produção ela fica **desligada** (`API_DOCS_ENABLED=false`) e as rotas exigem autenticação.
* **Limite de requisições:** `RateLimitingFilter` por IP, em camadas, e limite de e-mails por destinatário (ver
  [`tecnico/autenticacao.md`](../tecnico/autenticacao.md#limite-de-requisições)).
* **Privados:** Todos os outros endpoints exigem autenticação por padrão (`anyRequest().authenticated()`).

### 4. Senhas

* **NUNCA** armazene senhas em texto plano.
* Utilize o `BCryptPasswordEncoder` (já configurado no Spring Security) para hashear as senhas antes de salvar no banco.
* Ao validar login, compare o hash da senha enviada com o hash do banco usando `passwordEncoder.matches()`.

## O que NÃO fazer

* **NUNCA** commite chaves secretas (JWT Secret, senhas de banco) no Git. Use variáveis de ambiente (`System.getenv()`).
* **NUNCA** troque a autenticação por **cookie de sessão** (ou mova o access token para cookie) sem reativar o CSRF.
  O CSRF está desligado de propósito; veja abaixo.

## Por que o CSRF está desligado

CSRF é quando um **site de terceiros** faz o navegador da vítima enviar uma requisição para a nossa API e o navegador
anexa **sozinho** a credencial dela. Só funciona com credencial que o navegador envia automaticamente: cookie.

| Credencial | Como chega | Risco de CSRF |
|---|---|---|
| Access token (JWT) | Cabeçalho `Authorization: Bearer`, colocado pelo front via JavaScript | **Nenhum**: outro site não consegue ler o token nem fazer o navegador anexá-lo |
| Refresh token | Cookie `HttpOnly` em `/user/refresh` e `/user/logout` | Coberto: `SameSite=Strict` em produção faz o navegador **não** enviar o cookie em requisição vinda de outro site; e o CORS só libera as origens do front, então outro site não lê a resposta do refresh |

Por isso o `.csrf(AbstractHttpConfigurer::disable)` em `SecurityConfigurations` é seguro: todas as rotas protegidas
exigem o cabeçalho `Authorization`, e as duas rotas que usam cookie estão cobertas pelo `SameSite=Strict`. Ligar o
CSRF do Spring exigiria que o front buscasse e reenviasse um token CSRF em todo `POST/PUT/DELETE`, sem ganho real.

Isso deixa de valer se:
- o cookie de refresh passar a usar `SameSite=None` (necessário se o front ficar em **outro domínio** que a API).
  Nesse caso, reative o CSRF ao menos em `/user/refresh` e `/user/logout`, ou volte a servir front e API no mesmo
  domínio (o desenho atual, com Caddy: ver [`frontend-deploy.md`](../operacao/frontend-deploy.md));
- a autenticação passar a usar cookie de sessão ou o access token for para cookie.

No SonarCloud, o alerta "Make sure disabling Spring Security's CSRF protection is safe here" é um *security
hotspot*: deve ser revisado e marcado como **Safe**, com a justificativa acima.
* **NUNCA** crie seu próprio algoritmo de criptografia. Use os padrões da indústria (BCrypt, Argon2).

## Exemplo

**Controller com autenticação obrigatória:**

```java

@RestController
@RequestMapping("/users")
public class UserController {

    // Exige apenas estar autenticado
    @GetMapping("/me")
    public UserResponse getMyProfile(@AuthenticationPrincipal User user) {
        // ...
    }

    // Today: acessível por qualquer usuário autenticado.
    // Quando a autorização por papel existir, vira:
    //   @PreAuthorize("hasRole('ADMIN')")
    @GetMapping
    public List<UserResponse> listAll() {
        // ...
    }
}
```

**Configuração (`SecurityConfigurations`):**

```java

@Bean
public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
    http
            .csrf(AbstractHttpConfigurer::disable)
            .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .authorizeHttpRequests(auth -> auth
                    .requestMatchers(HttpMethod.POST, "/user/login").permitAll()  // Públicos
                    .requestMatchers("/swagger-ui/**", "/v3/api-docs/**").permitAll()
                    .anyRequest().authenticated()  // O resto exige autenticação
            )
            .addFilterBefore(securityFilter, UsernamePasswordAuthenticationFilter.class);

    return http.build();
}
```
