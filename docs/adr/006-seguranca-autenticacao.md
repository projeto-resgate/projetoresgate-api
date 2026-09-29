---
name: seguranca-autenticacao
status: aceito
summary: JWT Auth0 stateless via Authorization Bearer. Autorização por role ainda não existe: nenhum @PreAuthorize no código.
---

# 6. Segurança e autenticação

## Decisão

Spring Security com **JWT** (Auth0) para autenticação. A API é stateless: nada de sessão no
servidor, e o cliente manda `Authorization: Bearer <token>` em toda requisição protegida.

### 1. Autenticação (Stateless)
A API é **stateless**, ou seja, não mantém sessão no servidor. O cliente deve enviar um token JWT válido no cabeçalho `Authorization` de cada requisição protegida.

*   **Fluxo:**
    1.  Cliente envia credenciais (email/senha) para `/auth/login`.
    2.  Servidor valida e retorna um JWT assinado.
    3.  Cliente armazena o JWT (ex: LocalStorage) e o envia nas próximas requisições: `Authorization: Bearer <token>`.

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
*   **Públicos:** Login, cadastro, recuperação de senha, confirmação de e-mail e documentação (Swagger) são liberados explicitamente em `SecurityConfigurations`.
*   **Privados:** Todos os outros endpoints exigem autenticação por padrão (`anyRequest().authenticated()`).

### 4. Senhas
*   **NUNCA** armazene senhas em texto plano.
*   Utilize o `BCryptPasswordEncoder` (já configurado no Spring Security) para hashear as senhas antes de salvar no banco.
*   Ao validar login, compare o hash da senha enviada com o hash do banco usando `passwordEncoder.matches()`.

## O que NÃO fazer

*   **NUNCA** commite chaves secretas (JWT Secret, senhas de banco) no Git. Use variáveis de ambiente (`System.getenv()`).
*   **NUNCA** desabilite o CSRF se a API for consumida por navegadores (embora para APIs REST stateless, muitas vezes seja desabilitado, entenda o risco).
*   **NUNCA** crie seu próprio algoritmo de criptografia. Use os padrões da indústria (BCrypt, Argon2).

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
