# Autenticação e sessão

Como alguém entra, permanece logado e sai do sistema. Decisão de arquitetura em
[ADR 006](../adr/006-seguranca-autenticacao.md); este documento descreve o **comportamento** e os fluxos.

## Resumo

| Item                    | Valor                                                                                             |
|-------------------------|---------------------------------------------------------------------------------------------------|
| Esquema                 | JWT stateless no header `Authorization: Bearer <accessToken>`                                     |
| Access token            | Assinado HS256 (`api.security.token.secret`), emissor `projetoresgate_api`, **15 min** (900 s)    |
| Refresh token           | Valor aleatório, guardado como **hash** no banco, entregue em **cookie `refreshToken`**, **7 dias**|
| Cookie                  | `HttpOnly`, `Secure` e `SameSite=Strict` em produção (`api.cookie.*`)                             |
| Senha                   | BCrypt                                                                                            |
| Rate limit              | Por IP, em 3 camadas (login/senha: **5/min**; rotas públicas de escrita: **20/min**; geral: **300/min**) e por e-mail (**3/hora**). Ver [Limite de requisições](#limite-de-requisições) |
| Autorização por perfil  | **Não existe.** Só há o papel `ADMIN` e nada o consulta (ver [RFC 0001](../rfc/0001-autorizacao-por-role.md)) |

Os tempos são configuráveis por variável de ambiente: `API_SECURITY_ACCESS_TOKEN_DURATION`,
`API_SECURITY_REFRESH_TOKEN_DURATION`. As variáveis do limite de requisições estão [mais abaixo](#variáveis-do-limite).

## Rotas públicas

Tudo exige token, **exceto**:

| Rota                                              | Para quê                              |
|---------------------------------------------------|---------------------------------------|
| `POST /user`                                      | Criar usuário — **só público quando `api.user-registration.public=true` (dev)**; em produção exige login |
| `POST /user/login`, `/user/refresh`               | Entrar / renovar                      |
| `POST /user/forgot-password`, `/user/reset-password` | Recuperar senha                    |
| `POST /natural-person`                            | Cadastro de pessoa física (**ver aviso**) |
| `POST /natural-person/request-email-confirmation`, `/natural-person/confirm-email/{token}` | Confirmar e-mail |
| `/swagger-ui/**`, `/v3/api-docs/**`               | Documentação                          |

> **`POST /user` e produção.** Todo usuário criado nasce `ADMIN`, então a rota **não pode ser pública em
> produção**. Ela só é liberada sem login quando `api.user-registration.public=true`: o perfil `dev` liga,
> e a produção usa `API_USER_REGISTRATION_PUBLIC` (padrão **`false`**). Desligada, `POST /user` devolve
> **401** sem login e continua funcionando para quem está autenticado. O primeiro administrador em produção
> é criado por SQL: ver [runbook](../operacao/runbook.md#criar-o-primeiro-administrador).
>
> ⚠️ `POST /natural-person` segue público e dispara e-mail.

## Fluxo: login

```mermaid
sequenceDiagram
    autonumber
    participant F as Front
    participant RL as RateLimitingFilter
    participant API as UserController
    participant S as AuthenticateUserService
    participant DB as PostgreSQL

    F->>RL: POST /user/login {email, password}
    RL->>RL: gasta uma ficha do IP (camada AUTH)
    alt acima do limite
        RL-->>F: 429 Too Many Requests
    else dentro do limite
        RL->>API: segue
        API->>S: handle(email, password)
        S->>DB: findByEmail
        alt usuário inexiste OU senha errada
            S-->>F: 401 (BadCredentialsException, mesma resposta nos dois casos)
        else ok
            S->>S: gera access token (JWT com tokenVersion)
            S->>DB: grava hash do refresh token
            API-->>F: 200 {accessToken, expiresIn, user...}<br/>Set-Cookie: refreshToken (HttpOnly)
        end
    end
```

Usuário inexistente e senha errada retornam a **mesma** resposta, para não revelar quais e-mails existem.

## Fluxo: requisição autenticada

```mermaid
flowchart TD
    A[Request com Authorization: Bearer] --> B{Token presente?}
    B -- não --> Z[Segue sem autenticação]
    B -- sim --> C{Assinatura, emissor e validade OK?}
    C -- não --> X[Limpa contexto, loga warn]
    C -- sim --> D[Carrega User pelo sub]
    D --> E{User existe e<br/>tokenVersion do JWT ==<br/>tokenVersion do User?}
    E -- não --> X
    E -- sim --> F[Autentica a requisição]
    Z --> G{Rota é pública?}
    X --> G
    F --> G
    G -- sim --> H[Processa]
    G -- não, e não autenticado --> I[401 Unauthorized]
    F --> H
```

O filtro **nunca responde erro sozinho**: se o token é inválido, apenas deixa a requisição anônima, e quem
barra é a regra `anyRequest().authenticated()`.

## Fluxo: renovar sessão (refresh com rotação)

```mermaid
sequenceDiagram
    autonumber
    participant F as Front
    participant API as UserController
    participant S as RefreshTokenServiceImpl
    participant DB as PostgreSQL

    F->>API: POST /user/refresh (cookie refreshToken)
    alt sem cookie
        API-->>F: 400 "Cookie de renovação não fornecido"
    else com cookie
        API->>S: handle(token)
        S->>DB: busca por hash
        alt não encontrado ou expirado
            S-->>F: 400
        else válido
            S->>DB: cria NOVO refresh token
            S->>DB: apaga o refresh token ANTIGO
            API-->>F: 200 {novo accessToken}<br/>Set-Cookie: novo refreshToken
        end
    end
```

**Rotação:** cada refresh invalida o anterior. Reusar um refresh token antigo falha.

## Fluxo: logout

| Endpoint            | Efeito                                                                                                       |
|---------------------|--------------------------------------------------------------------------------------------------------------|
| `POST /user/logout` | Apaga **este** refresh token e limpa o cookie. O access token continua válido até expirar (≤ 15 min).        |
| `POST /user/logout-all` | Apaga **todos** os refresh tokens do usuário **e incrementa `tokenVersion`** → todos os access tokens já emitidos passam a ser rejeitados na hora. Exige estar autenticado. |

`tokenVersion` é o mecanismo de revogação imediata: o JWT carrega a versão; se não bate com a do banco,
o filtro descarta o token.

## Fluxo: esqueci minha senha

```mermaid
sequenceDiagram
    autonumber
    participant U as Usuário
    participant F as Front
    participant API as API
    participant M as SMTP (Gmail)

    U->>F: informa e-mail
    F->>API: POST /user/forgot-password {email}
    API->>API: e-mail existe? (se não, não faz nada)
    API->>API: apaga token anterior, gera token novo (hash no banco, 10 min)
    API->>M: envia e-mail com link de reset
    API-->>F: 200 (sempre, exista o e-mail ou não)
    U->>F: abre o link e digita a nova senha
    F->>API: POST /user/reset-password {token, newPassword}
    alt token inválido
        API-->>F: 400 "Token inválido ou não encontrado."
    else token expirado
        API->>API: apaga o token
        API-->>F: 400 "O token expirou..."
    else senha < 6 caracteres
        API-->>F: 400 "A nova senha deve ter no mínimo 6 caracteres."
    else ok
        API->>API: troca a senha, apaga o token
        API-->>F: 200
    end
```

- A resposta é **sempre 200** em `forgot-password`, para não revelar se o e-mail existe.
- O token vale **10 minutos** e é de uso único.
- ⚠️ Trocar a senha **não** invalida sessões existentes (não incrementa `tokenVersion`).
- O link do e-mail é `<FRONTEND_URL>/reset-password?token=...` (propriedade `app.frontend-url`; a barra final é ignorada).

## Fluxo: confirmação de e-mail (pessoa física)

```mermaid
sequenceDiagram
    autonumber
    participant API as API
    participant M as SMTP
    participant P as Pessoa

    Note over API: Ao criar NaturalPerson, a API já dispara o pedido
    API->>API: gera token (24 h), guarda hash
    API->>M: e-mail "Confirme seu E-mail"
    P->>API: POST /natural-person/confirm-email/{token}
    alt token inválido / expirado
        API-->>P: 400
    else ok
        API->>API: is_email_verified = true, apaga token
        API-->>P: 200
    end
```

- Pode-se pedir novo e-mail em `POST /natural-person/request-email-confirmation`. Se o e-mail não existe
  ou **já está verificado**, nada acontece (silencioso).
- O token vale **24 horas**.
- O link do e-mail é `<FRONTEND_URL>/confirm-email/<token>` (mesma propriedade `app.frontend-url` do reset de senha).
- O e-mail verificado é só informativo hoje: nada no sistema bloqueia quem não confirmou.

## Limite de requisições

Protege contra força bruta, spam e varredura. Implementado em `RateLimitingFilter` (por IP) e
`EmailRateLimiter` (por destinatário), sobre o mesmo algoritmo, `TokenBucketRateLimiter`.

### Camadas por IP

Cada requisição cai na camada **mais específica** que a descreve e gasta uma ficha só dela:

| Camada      | Rotas                                                                                              | Padrão       |
|-------------|----------------------------------------------------------------------------------------------------|--------------|
| **AUTH**    | `POST /user/login`, `/user/refresh`, `/user/forgot-password`, `/user/reset-password`               | **5 / min**  |
| **PUBLIC**  | `POST /user`, `POST /natural-person`, `/natural-person/request-email-confirmation`, `/natural-person/confirm-email/**` | **20 / min** |
| **GENERAL** | Todo o resto                                                                                       | **300 / min** |

Em dev os padrões são mais folgados (AUTH 20, PUBLIC 100, GENERAL 1000).

```mermaid
flowchart TD
    A[Requisição] --> B{OPTIONS<br/>preflight CORS?}
    B -- sim --> OK[Segue, sem gastar ficha]
    B -- não --> C{Limite desligado?}
    C -- sim --> OK
    C -- não --> D[Descobre o IP do cliente]
    D --> E{Qual camada?}
    E -- "login, refresh,<br/>forgot/reset" --> F[AUTH]
    E -- "criar usuário/pessoa,<br/>confirmar e-mail" --> G[PUBLIC]
    E -- "demais rotas" --> H[GENERAL]
    F --> I{Tem ficha<br/>no balde do IP?}
    G --> I
    H --> I
    I -- sim --> J[Gasta 1 ficha e segue<br/>para a autenticação]
    I -- não --> K["429 + Retry-After + JSON"]
```

### Como funciona (token bucket)

- Cada IP tem um **balde** por camada, com capacidade igual ao limite (ex.: 5). Cada requisição gasta 1 ficha.
- As fichas voltam **continuamente**: `limite ÷ janela` por segundo (5 por minuto = 1 ficha a cada 12 s). Dá para
  fazer uma pequena rajada e, depois, só no ritmo permitido, sem o "estouro" de 2x que janelas fixas permitem
  na virada do minuto.
- O filtro fica **dentro** da cadeia do Spring Security, logo **depois do CORS** e **antes da autenticação**.
  Por isso: o 429 sai com cabeçalhos CORS (o front consegue ler a resposta) e requisições sem token também contam.
- **Preflight (`OPTIONS`) não gasta ficha**: o navegador o envia antes de cada chamada.

### Resposta ao estourar

```http
HTTP/1.1 429
Retry-After: 9
Content-Type: application/json

{"code":"Too Many Requests","message":"Muitas requisições. Tente novamente em 9 segundos."}
```

`Retry-After` é o tempo, em segundos, até voltar uma ficha. O front deve exibi-lo ou esperar esse tempo.

### Limite por e-mail

Além do IP, um mesmo **destinatário** recebe no máximo **3 e-mails por hora** (reset de senha e confirmação de
e-mail). Impede que alguém com vários IPs encha a caixa de uma pessoa e esgote a cota do Gmail.

Quando o limite é atingido, o pedido é **ignorado em silêncio**: a resposta é igual à de sempre (200), sem enviar
nada. Assim ninguém descobre que o limite existe nem se o e-mail está cadastrado.

### Qual é o IP do cliente

| Situação                              | IP usado                                                                              |
|---------------------------------------|---------------------------------------------------------------------------------------|
| `trusted-proxies = 0` (**padrão**)    | O IP da conexão (`remoteAddr`). `X-Forwarded-For` é **ignorado**                      |
| `trusted-proxies = N` (N > 0)         | A **N-ésima entrada de `X-Forwarded-For`, contada da direita** (a que o proxy mais externo acrescentou). Se o cabeçalho for mais curto que N, cai para o `remoteAddr` |

`X-Forwarded-For` pode ser **forjado por qualquer cliente**: confiar nele sem um proxy na frente deixaria qualquer
um burlar o limite trocando o cabeçalho. Hoje a API é exposta direto (sem proxy), então o padrão é 0. **Quando
colocar um proxy reverso (Caddy/Nginx) na frente, defina `API_RATE_LIMIT_TRUSTED_PROXIES=1`**; sem isso, todos os
usuários passariam a compartilhar o IP do proxy e o limite valeria para o conjunto.

### Variáveis do limite

| Variável                                             | Padrão  | Descrição                                                  |
|------------------------------------------------------|---------|------------------------------------------------------------|
| `API_RATE_LIMIT_ENABLED`                             | `true`  | `false` desliga o limite (por IP e por e-mail)             |
| `API_RATE_LIMIT_MAX_REQUESTS` / `_WINDOW_SECONDS`    | 5 / 60  | Camada AUTH                                                |
| `API_RATE_LIMIT_PUBLIC_MAX_REQUESTS` / `_WINDOW_SECONDS` | 20 / 60 | Camada PUBLIC                                          |
| `API_RATE_LIMIT_GENERAL_MAX_REQUESTS` / `_WINDOW_SECONDS` | 300 / 60 | Camada GENERAL                                        |
| `API_RATE_LIMIT_EMAIL_MAX_REQUESTS` / `_WINDOW_SECONDS` | 3 / 3600 | E-mails por destinatário                                |
| `API_RATE_LIMIT_TRUSTED_PROXIES`                     | `0`     | Quantos proxies confiáveis existem na frente da API        |

### Limitações conhecidas

- **Em memória, por instância:** reiniciar a API zera os baldes, e várias réplicas teriam limites independentes.
  Para uma única VM basta; com réplicas seria preciso um armazenamento compartilhado (ex.: Redis).
- **Atrás de NAT** (vários usuários num mesmo IP, como um escritório) todos dividem o mesmo balde. Se isso incomodar,
  aumente os limites PUBLIC e GENERAL.
- **Memória limitada:** baldes ociosos são removidos e há um teto de 100 mil chaves; acima dele, IPs novos passam
  sem controle (prefere-se disponibilidade a derrubar a aplicação).
- O limite **não substitui** autenticação nem captcha: reduz o ritmo de um ataque, não o impede.

## Limpeza de tokens expirados

`RefreshTokenCleanupTask` roda a cada hora (`api.security.refresh-token.cleanup.rate`, 3 600 000 ms) e apaga
refresh tokens vencidos. Pode ser desligado com `API_SECURITY_REFRESH_TOKEN_CLEANUP_ENABLED=false`.

## Variáveis de ambiente relacionadas

| Variável                                   | Padrão   | Observação                                          |
|--------------------------------------------|----------|-----------------------------------------------------|
| `API_SECURITY_TOKEN_SECRET`                | —        | **Obrigatória.** Troque ao suspeitar de vazamento (derruba todas as sessões). |
| `API_SECURITY_ACCESS_TOKEN_DURATION`       | `900`    | Segundos.                                           |
| `API_SECURITY_REFRESH_TOKEN_DURATION`      | `604800` | Segundos. Também é o `Max-Age` do cookie.           |
| `API_COOKIE_SECURE`                        | `true`   | Exige HTTPS. Em HTTP puro o navegador ignora o cookie. |
| `API_COOKIE_SAME_SITE`                     | `Strict` | Se front e API ficarem em domínios diferentes, será preciso `None` (+ `Secure`). |
| `FRONTEND_URL`                             | —        | **Obrigatória em produção.** URL base do front, usada nos links dos e-mails (ex.: `https://app.exemplo.org`). Em dev é `http://localhost:5173`. |
| `API_USER_REGISTRATION_PUBLIC`             | `false`  | `true` libera `POST /user` **sem login**. Nunca ligar em produção. |
| `CORS-ALLOWED-ORIGINS`                     | —        | Origem(ns) do front. Nome **com hífen** (ver [deploy](../operacao/infraestrutura-deploy.md)). |
