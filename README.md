# Projeto Resgate API

API RESTful desenvolvida para o sistema **Projeto Resgate**. Esta aplicação gerencia autenticação, controle de usuários e perfis de acesso, servindo como backend para aplicações web e mobile.

<details>
<summary><strong style="font-size:1.5em">🚀 Tecnologias Utilizadas</strong></summary>

*   **Java 21**
*   **Spring Boot 3.3.1**
*   **Spring Security + JWT (Auth0)**
*   **PostgreSQL** (Banco de Dados)
*   **Flyway** (Migração de Banco de Dados)
*   **Docker & Docker Compose**
*   **SpringDoc OpenAPI (Swagger)** (Documentação)
*   **JavaMailSender** (Envio de E-mails)

</details>

<details>
<summary><strong style="font-size:1.5em">🏗️ Arquitetura e Design</strong></summary>

O projeto adota **Clean Architecture** e princípios de **DDD (Domain-Driven Design)** para isolar regras de negócio de detalhes de infraestrutura.

### 📄 Documentação de Decisões Arquiteturais (ADR)

Para mais detalhes sobre as decisões arquiteturais e padrões adotados, consulte nossos ADRs:

*   [001 - Padrões Arquiteturais e Estrutura do Projeto](docs/adr/001-padroes-arquiteturais.md)
*   [002 - Estratégia de Tratamento de Erros](docs/adr/002-tratamento-de-erros.md)
*   [003 - Gerenciamento de Banco de Dados e Migrations](docs/adr/003-gerenciamento-banco-dados.md)
*   [004 - Estratégia de Testes](docs/adr/004-estrategia-testes.md)
*   [005 - Padrões de Nomenclatura e Idioma](docs/adr/005-padroes-nomenclatura-idioma.md)
*   [006 - Segurança e Autenticação](docs/adr/006-seguranca-autenticacao.md)

</details>

<details>
<summary><strong style="font-size:1.5em">💡 Dicas antes de commitar</strong></summary>

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
- [ ] Cobertura >= 80%
- [ ] Code review solicitado
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
- [ ] Está utilizando DTOs (Request/Response) para se comunicar com mundo exterior
- [ ] Unit tests (80%+ cobertura)
- [ ] Integration tests
- [ ] Swagger documentado

---
</details>

<details>
<summary><strong style="font-size:1.5em">⚙️ Configuração e Execução</strong></summary>

### 1. Pré-requisitos e Banco de Dados (Docker)

**Pré-requisitos**
*   Java 21
*   Maven
*   Docker e Docker Compose

**Subindo o Banco de Dados**
Utilize o Docker Compose para subir o container do PostgreSQL.

```bash
docker-compose up -d
```
Isso iniciará o banco na porta `5432`.

### 2. Configuração no IntelliJ IDEA (Padrão da Equipe)

Para garantir que todos na equipe rodem o projeto com as mesmas configurações, crie um template de execução:

1.  Vá em **Run** > **Edit Configurations...**.
2.  Clique no **+** e selecione **Application**.
3.  **Name:** `Start`
4.  **Main class:** `ProjetoResgateApiApplication`
5.  **Program arguments:** `--spring.profiles.active=dev`
6.  **(Opcional) Environment variables:**
    *   `MAIL_USERNAME=seu_email`
    *   `MAIL_PASSWORD=sua_senha_app`
7.  Clique em **Apply** e **OK**.
8.  Execute a configuração `Start`.

### 3. Docker Build & Run
A aplicação usa um build em dois estágios para gerar uma imagem leve.

```bash
docker build -t projetoresgate-api .
docker run -p 8080:8080 projetoresgate-api
```

</details>

<details>
<summary><strong style="font-size:1.5em">📚 Documentação da API</strong></summary>

Acesse a documentação interativa com a aplicação rodando:

👉 **[Swagger UI](http://localhost:8080/swagger-ui.html)**
<br>
👉 **[JSON Docs](http://localhost:8080/v3/api-docs)**

### 🔐 Como Autenticar no Swagger

1.  Crie um usuário no endpoint `POST /user`.
2.  Faça login no endpoint `POST /user/login`.
3.  Copie o `access_token` retornado.
4.  No Swagger, clique no botão **Authorize** (cadeado).
5.  Cole o token `Bearer seu_token`.

</details>

<details>
<summary><strong style="font-size:1.5em">📈 Diagramas e Documentação Visual</strong></summary>

Utilizamos **PlantUML** para gerar diagramas diretamente do código, mantendo a documentação visual sempre sincronizada com a arquitetura.

### Diagrama de Classes
O principal diagrama que representa nosso modelo de domínio completo está localizado em:
*   [`docs/class-diagram.puml`](docs/class-diagram.puml)

Este arquivo descreve as entidades, seus atributos e os relacionamentos entre elas, servindo como uma referência central para entender a estrutura do banco de dados e das classes de domínio.

### 🔧 Como Visualizar os Diagramas (.puml)

Para visualizar e editar os arquivos `.puml` diretamente na sua IDE, é necessário instalar um plugin.

#### No IntelliJ IDEA (Recomendado)
1.  Vá em `File` > `Settings` (ou `Ctrl+Alt+S`).
2.  Selecione a aba **Plugins**.
3.  Busque por **"PlantUML integration"** e instale-o.
4.  Após a instalação, reinicie a IDE.
5.  Abra qualquer arquivo `.puml` e utilize a janela de preview para ver o diagrama renderizado.

#### No VS Code
1.  Vá para a aba de **Extensions** (ou `Ctrl+Shift+X`).
2.  Busque por **"PlantUML"** (por `jebbs`).
3.  Instale a extensão.
4.  Com um arquivo `.puml` aberto, use o atalho `Alt+D` para abrir o preview do diagrama.

</details>
