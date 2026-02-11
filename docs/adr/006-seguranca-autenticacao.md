# 6. Segurança e Autenticação

Data: 2026-02-10

## Contexto

A segurança é um aspecto crítico em qualquer aplicação, especialmente em projetos comunitários onde dados sensíveis podem ser expostos. Desenvolvedores iniciantes podem ter dificuldade em entender como proteger endpoints e gerenciar permissões.

## Decisão

Adotamos o **Spring Security** com **JWT (JSON Web Token)** para autenticação e autorização.

### 1. Autenticação (Stateless)
A API é **stateless**, ou seja, não mantém sessão no servidor. O cliente deve enviar um token JWT válido no cabeçalho `Authorization` de cada requisição protegida.

*   **Fluxo:**
    1.  Cliente envia credenciais (email/senha) para `/auth/login`.
    2.  Servidor valida e retorna um JWT assinado.
    3.  Cliente armazena o JWT (ex: LocalStorage) e o envia nas próximas requisições: `Authorization: Bearer <token>`.

### 2. Autorização (Roles e Permissions)
Utilizamos anotações `@PreAuthorize` nos Controllers para restringir o acesso com base nas roles do usuário.

*   **Roles:** Definidas no Enum `Role` (ex: `ADMIN`, `USER`, `VOLUNTEER`).
*   **Anotação:** `@PreAuthorize("hasRole('ADMIN')")` ou `@PreAuthorize("hasAnyRole('ADMIN', 'VOLUNTEER')")`.

### 3. Proteção de Endpoints
*   **Públicos:** Endpoints de login, cadastro (se aberto), recuperação de senha e documentação (Swagger) devem ser explicitamente liberados no `SecurityConfig`.
*   **Privados:** Todos os outros endpoints devem exigir autenticação por padrão (`anyRequest().authenticated()`).

### 4. Senhas
*   **NUNCA** armazene senhas em texto plano.
*   Utilize o `BCryptPasswordEncoder` (já configurado no Spring Security) para hashear as senhas antes de salvar no banco.
*   Ao validar login, compare o hash da senha enviada com o hash do banco usando `passwordEncoder.matches()`.

## O que NÃO fazer

*   **NUNCA** commite chaves secretas (JWT Secret, senhas de banco) no Git. Use variáveis de ambiente (`System.getenv()`).
*   **NUNCA** desabilite o CSRF se a API for consumida por navegadores (embora para APIs REST stateless, muitas vezes seja desabilitado, entenda o risco).
*   **NUNCA** crie seu próprio algoritmo de criptografia. Use os padrões da indústria (BCrypt, Argon2).

## Exemplo

**Controller Protegido:**
```java
@RestController
@RequestMapping("/users")
public class UserController {

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')") // Apenas ADMIN pode listar todos
    public List<UserResponse> listAll() {
        // ...
    }

    @GetMapping("/me")
    @PreAuthorize("isAuthenticated()") // Qualquer usuário logado
    public UserResponse getMyProfile(@AuthenticationPrincipal User user) {
        // ...
    }
}
```

**Configuração (SecurityConfig):**
```java
@Bean
public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
    http
        .csrf(AbstractHttpConfigurer::disable)
        .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
        .authorizeHttpRequests(auth -> auth
            .requestMatchers("/auth/**", "/swagger-ui/**").permitAll() // Públicos
            .anyRequest().authenticated() // O resto é privado
        )
        .addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class);
    
    return http.build();
}
```
