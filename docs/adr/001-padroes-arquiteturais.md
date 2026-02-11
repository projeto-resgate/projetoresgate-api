# 1. Padrões Arquiteturais e Estrutura do Projeto

Data: 2026-02-10

## Contexto

O projeto `projetoresgate-api` necessita de uma estrutura clara e modular para garantir a manutenibilidade, testabilidade e escalabilidade do código. A arquitetura deve separar as responsabilidades de forma lógica, isolando o domínio da aplicação de detalhes de infraestrutura e interfaces externas.

## Decisão

Adotamos uma arquitetura baseada em **Clean Architecture**, organizando o código em camadas com responsabilidades bem definidas e princípios de **DDD (Domain-Driven Design)**. A estrutura principal divide-se em `core` (domínio e regras de negócio) e `infrastructure` (detalhes técnicos e frameworks).

Dentro do `core`, organizamos o código por **funcionalidades** (ex: `user`), e dentro de cada funcionalidade, aplicamos os seguintes padrões e componentes:

### 1. Controller (`api`)
*   **Responsabilidade:** Ponto de entrada da aplicação (ex: REST API). Recebe as requisições HTTP, valida os dados de entrada (DTOs), invoca o Caso de Uso apropriado e retorna a resposta (DTOs) com o código HTTP adequado.
*   **Localização:** `core/{feature}/api`
*   **Sufixo:** `Controller` (ex: `UserController`)

### 2. Request & Response DTOs (`api/dto`)
*   **Responsabilidade:** Objetos de Transferência de Dados (Data Transfer Objects) usados para comunicação entre o cliente e a API.
    *   **Request:** Dados enviados pelo cliente para a API.
    *   **Response:** Dados retornados pela API para o cliente.
*   **Localização:** `core/{feature}/api/dto`
*   **Nomenclatura:** `{Acao}Request`, `{Entidade}Response` (ex: `CreateUserRequest`, `UserResponse`)

### 3. Use Case (`usecase`)
*   **Responsabilidade:** Define as interfaces das operações de negócio (regras de aplicação). Atua como uma porta de entrada para o domínio. Cada Caso de Uso representa uma ação específica que o sistema pode realizar.
*   **Localização:** `core/{feature}/usecase`
*   **Sufixo:** `UseCase` (ex: `CreateUserUseCase`, `FindUserUseCase`)
*   **Padrão:** Interface que define o contrato da operação.

### 4. Service (`service`)
*   **Responsabilidade:** Implementação concreta dos Casos de Uso. Contém a lógica de orquestração do negócio, chamando repositórios, outros serviços ou entidades de domínio.
*   **Localização:** `core/{feature}/service` (ou `core/{feature}/usecase/impl` dependendo da preferência, mas no projeto atual parece estar em `service` implementando a interface do `usecase`)
*   **Sufixo:** `Service` (ex: `CreateUserService` implementa `CreateUserUseCase`)

### 5. Command & Query (`usecase/command`, `usecase/query`)
*   **Responsabilidade:** Aplicamos o padrão **CQRS** (Command Query Responsibility Segregation) de forma simplificada nos parâmetros dos Casos de Uso.
    *   **Command:** Objeto que encapsula todos os dados necessários para realizar uma operação de escrita (criar, atualizar, deletar). Representa uma intenção de mudança de estado.
    *   **Query:** Objeto que encapsula os dados necessários para realizar uma operação de leitura (buscar, listar).
*   **Localização:**
    *   Commands: `core/{feature}/usecase/command`
    *   Queries: `core/{feature}/usecase/query`
*   **Sufixo:** `Command`, `Query` (ex: `CreateUserCommand`, `FindUserByIdQuery`)

### 6. Repository (`repository`)
*   **Responsabilidade:** Interface que define o contrato para acesso a dados (persistencia). O domínio define *o que* precisa ser salvo/buscado, e a infraestrutura implementa *como*.
*   **Localização:** `core/{feature}/repository`
*   **Sufixo:** `Repository` (ex: `UserRepository`)

### 7. Domain Entity (`domain`)
*   **Responsabilidade:** Representa os objetos fundamentais do negócio, contendo estado e comportamento (regras de negócio essenciais).
*   **Localização:** `core/{feature}/domain`

## Consequências

### Positivas
*   **Separação de Responsabilidades:** Cada componente tem um papel claro, facilitando o entendimento e a manutenção.
*   **Testabilidade:** A lógica de negócio (Use Cases/Services) pode ser testada unitariamente sem depender de frameworks externos (como Spring MVC ou Banco de Dados), usando mocks para as interfaces (Repositories).
*   **Independência de Frameworks:** O `core` da aplicação não deve depender fortemente de frameworks, facilitando atualizações ou trocas de tecnologia.
*   **Padronização:** A estrutura uniforme entre as features facilita a navegação de novos desenvolvedores no projeto.

### Negativas
*   **Verbosidade:** A criação de muitas classes (DTOs, Commands, Interfaces, Implementações) pode parecer excessiva para operações CRUD simples.
*   **Curva de Aprendizado:** Desenvolvedores não familiarizados com Clean Architecture podem levar um tempo para entender onde colocar cada lógica.

## Exemplo de Fluxo

1.  **Cliente** envia POST para `/users` com JSON.
2.  **Controller** (`UserController`) recebe, converte JSON para `CreateUserRequest` (DTO).
3.  **Controller** converte `CreateUserRequest` para `CreateUserCommand`.
4.  **Controller** chama `CreateUserUseCase.execute(command)`.
5.  **Service** (`CreateUserService`) executa a lógica:
    *   Valida regras de negócio.
    *   Cria entidade `User` (Domain).
    *   Chama `UserRepository.save(user)`.
6.  **Service** retorna o resultado (ex: ID do usuário ou o próprio usuário).
7.  **Controller** converte o resultado para `UserResponse` (DTO) e retorna HTTP 201.
