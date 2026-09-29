# Projeto Resgate API

API RESTful desenvolvida para o sistema **Projeto Resgate**. Esta aplicação gerencia autenticação, controle de usuários
e perfis de acesso, servindo como backend para aplicações web e mobile.

<details>
<summary><strong style="font-size:1.5em">🚀 Tecnologias Utilizadas</strong></summary>

* **Java 21**
* **Spring Boot 3.4.4**
* **Spring Security + JWT (Auth0)**
* **PostgreSQL** (Banco de Dados)
* **Flyway** (Migração de Banco de Dados)
* **Docker & Docker Compose**
* **SpringDoc OpenAPI (Swagger)** (Documentação)
* **JavaMailSender** (Envio de E-mails)

</details>

<details>
<summary><strong style="font-size:1.5em">🏗️ Arquitetura e Design</strong></summary>

O projeto adota **Clean Architecture** e princípios de **DDD (Domain-Driven Design)** para isolar regras de negócio de
detalhes de infraestrutura.

### 📚 Documentação

Comece pelo índice: **[`docs/README.md`](docs/README.md)**.

| Documento | Para quê |
| --- | --- |
| [`AGENTS.md`](AGENTS.md) | Regras e comandos. **Leia antes de escrever código.** |
| [`docs/architecture.md`](docs/architecture.md) | Onde cada tipo de classe mora e por quê |
| [`docs/convencoes.md`](docs/convencoes.md) | Padrões práticos com código real para copiar |
| [`docs/testes.md`](docs/testes.md) | Como os testes são organizados e executados |
| [`docs/banco-de-dados.md`](docs/banco-de-dados.md) | Migrations, nomenclatura SQL e ambiente local |
| [`docs/adr/`](docs/adr/README.md) | Decisões arquiteturais e o porquê de cada uma |
| [`docs/rfc/`](docs/rfc/README.md) | Propostas em discussão |

### 📄 Decisões Arquiteturais (ADR)

As decisões estão em [`docs/adr/README.md`](docs/adr/README.md) — a tabela lá é a fonte única de
índice. Se um ADR divergir do código, o ADR está errado e deve ser atualizado.

## Branch Naming

```
feature/nome-descritivo     # Nova feature
fix/bug-description          # Correção
docs/assunto                 # Documentação
test/teste                   # Testes
refactor/mudanca            # Refatoração
```

## Commits (Conventional Commits)

```
feat(user): adicionar email confirmation
fix(auth): corrigir jwt expiration
docs: atualizar README
test(user): aumentar cobertura para 90%
refactor(service): simplificar validação
```

## Pull Request

**Título:** Siga o padrão de commits acima

**Descrição:**

```
## O que mudou
Breve descrição.

## Por quê
Por que foi feito.

## Tipos de mudança
- [x] Nova feature
- [ ] Bug fix
- [ ] Breaking change
- [ ] Documentação

## Checklist
- [ ] Testes adicionados
- [ ] Documentação atualizada
- [ ] Cobertura dos caminhos principais mantida
- [ ] Code review solicitado
- [ ] `./mvnw test` passa (precisa de Docker)
- [ ] Se mudou entidade ou tabela: migration nova, Swagger e docs atualizados
```

## Code Style

### Nomenclatura

- **Classes:** PascalCase (`UserService`, `CreateUserCommand`)
- **Métodos:** camelCase (`handle()`, `findByEmail()`)
- **Constantes:** UPPER_SNAKE_CASE
- **Variáveis:** camelCase

## Checklist para fazer antes de um commit

- [ ] Regras de domínio na entidade
- [ ] Utilizou padrão service implementando usecase
- [ ] Está utilizando DTOs (`*Command` na entrada, `*Response` na saída) — ver [ADR 008](docs/adr/008-formato-dtos-api.md)
- [ ] Testes unitários (JUnit 5 + Mockito)
- [ ] Testes de controller (`@WebMvcTest`) e de integração (`@DataJpaTest` + `PostgresIntegrationTest`)
- [ ] Swagger documentado

---
</details>

<details>
<summary><strong style="font-size:1.5em">⚙️ Configuração e Execução</strong></summary>

### 1. Pré-requisitos e Banco de Dados (Docker)

**Pré-requisitos**

* Java 21
* Maven
* Docker e Docker Compose

**Subindo o Banco de Dados**
Utilize o Docker Compose para subir o container do PostgreSQL.

```bash
docker-compose up -d
```

Isso iniciará o banco na porta `5432`. O Compose sobe três serviços em cadeia: o banco, um
container do Flyway que aplica as migrations, e um seeder que roda `docker/database/init.sql`
depois que as migrations terminam.

Confira se o serviço de migrations terminou com sucesso:

```bash
docker compose ps -a
```

Se as migrations falharem, recrie do zero (isso **apaga os dados locais**):

```bash
docker compose down -v && docker compose up -d
```

Mais detalhes em [`docs/banco-de-dados.md`](docs/banco-de-dados.md).

### 2. Rodar os testes

```bash
./mvnw test          # suíte completa (precisa de Docker)
./mvnw -o test       # idem, offline: bem mais rápido no dia a dia
./mvnw test -Dtest=FamilyGroupServiceIntegrationTest   # uma classe
```

Os testes de integração sobem um PostgreSQL real via Testcontainers — não há H2 no projeto. Por
isso o Docker precisa estar no ar. Detalhes em [`docs/testes.md`](docs/testes.md).

### 3. Configuração no IntelliJ IDEA (Padrão da Equipe)

Para garantir que todos na equipe rodem o projeto com as mesmas configurações, crie um template de execução:

1. Vá em **Run** > **Edit Configurations...**.
2. Clique no **+** e selecione **Application**.
3. **Name:** `Start`
4. **Main class:** `ProjetoResgateApiApplication`
5. **Program arguments:** `--spring.profiles.active=dev`
6. **Environment variables:**
    * `SPRING_MAIL_USERNAME=seu_email`
    * `SPRING_MAIL_PASSWORD=sua_senha_app` (Para gerar, entre em [APP PASS](https://myaccount.google.com/apppasswords)
      digite ProjetoResgateApi e clique em criar)
7. Clique em **Apply** e **OK**.
8. Execute a configuração `Start`.

</details>

<details>
<summary><strong style="font-size:1.5em">📚 Documentação da API</strong></summary>

Acesse a documentação interativa com a aplicação rodando:

👉 **[Swagger UI](http://localhost:8080/swagger-ui.html)**
<br>
👉 **[JSON Docs](http://localhost:8080/v3/api-docs)**

### 🔐 Como Autenticar no Swagger

1. Crie um usuário no endpoint `POST /user`.
2. Faça login no endpoint `POST /user/login`.
3. Copie o `access_token` retornado.
4. No Swagger, clique no botão **Authorize** (cadeado).
5. Cole o token `Bearer seu_token`.

### 📐 Diagramas

Os diagramas de arquitetura estão em Mermaid, dentro de
[`docs/architecture.md`](docs/architecture.md). Editores com suporte a Mermaid (GitHub, VS Code
com extensão, IntelliJ com plugin) renderizam direto do texto.

</details>
