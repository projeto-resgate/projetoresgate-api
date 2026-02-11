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

### Estrutura de Pacotes

A organização segue Separando a aplicação em `core` (domínio) e `infrastructure`.

#### 1. `infrastructure`
Camada de suporte técnico e integração com frameworks. Contém configurações do Spring, Security, implementações de e-mail e tratamento global de exceções. O domínio não possui dependência desta camada.

#### 2. `core` (Domínio)
Contém os contextos delimitados (Bounded Contexts), como `user`. A estrutura interna reflete as camadas da arquitetura limpa:

*   **`domain`**: Entidades e Enums. Camada mais interna, contendo apenas lógica de negócio pura e estado, sem dependências de frameworks.
*   **`usecase`**: Interfaces e Commands que definem as operações do sistema (ex: `CreateUser`), seguindo o princípio de Segregação de Interfaces.
*   **`service`**: Implementação dos casos de uso. Orquestra o fluxo de dados, validações e chamadas aos repositórios.
*   **`repository`**: Interfaces para persistência de dados. O domínio define o contrato, e a infraestrutura provê a implementação (Inversão de Dependência).
*   **`api`**: Camada de entrada (Controllers e DTOs). Responsável pela conversão entre requisições HTTP e objetos de domínio. Utiliza Records para DTOs.

### Decisões Técnicas

*   **Java 21**: Uso de *Records* e *Pattern Matching*.
*   **Spring Boot 3.3**: Framework base para injeção de dependência e servidor web.
*   **Flyway**: Versionamento e migração de esquema de banco de dados.
*   **Docker**: Containerização do banco de dados para consistência entre ambientes.
*   **Spring Security + JWT**: Autenticação *stateless*.

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
