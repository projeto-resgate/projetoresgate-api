# Pipeline de CI/CD

Dois workflows no GitHub Actions, com responsabilidades separadas:

| Workflow                                          | Gatilho                                      | O que faz                                          |
|---------------------------------------------------|----------------------------------------------|----------------------------------------------------|
| [`ci.yml`](../../.github/workflows/ci.yml)        | push na `main`, PR, manual                   | Build, testes e análise no SonarCloud              |
| [`deploy.yml`](../../.github/workflows/deploy.yml)| **somente manual** (`workflow_dispatch`)     | Gera a imagem Docker e publica no Docker Hub       |

A **VM não é acessada pelo GitHub**. Quem atualiza o servidor é o **Watchtower**, rodando na própria VM, que
observa o Docker Hub e puxa a imagem nova. Detalhes em [`infraestrutura-deploy.md`](infraestrutura-deploy.md).

## Visão ponta a ponta

```mermaid
flowchart LR
    DEV[Dev] -->|push / PR| GH[GitHub]
    GH --> CI

    subgraph CI["ci.yml"]
        direction TB
        C1[Checkout<br/>histórico completo] --> C2[JDK 21 + cache Maven]
        C2 --> C3["./mvnw clean verify<br/>testes + Jacoco"]
        C3 --> C4[Sonar<br/>SonarCloud]
    end

    CI -->|✅ verde| MERGE[merge na main]
    MERGE -.->|você decide| TRIG[/Run workflow<br/>deploy.yml/]

    subgraph DEPLOY["deploy.yml (manual)"]
        direction TB
        D1[Checkout da ref] --> D2[Login Docker Hub]
        D2 --> D3[Build com cache GHA]
        D3 --> D4["Push<br/>:prod-sha e :latest"]
    end

    TRIG --> DEPLOY
    D4 --> HUB[(Docker Hub)]

    subgraph VM["VM da ONG"]
        W[Watchtower<br/>poll a cada 5 min] --> API[API container]
        API --> DB[(Postgres)]
    end

    HUB -->|detecta :latest novo| W
```

## `ci.yml` em detalhe

```mermaid
sequenceDiagram
    autonumber
    participant GH as GitHub Actions
    participant M as Maven
    participant TC as Testcontainers (Docker)
    participant SC as SonarCloud

    GH->>GH: checkout (fetch-depth 0)
    GH->>GH: setup-java 21 (Temurin) + cache ~/.m2
    GH->>M: ./mvnw -B clean verify
    M->>TC: sobe PostgreSQL real para os testes
    TC-->>M: testes de repository rodam com Flyway
    M->>M: gera target/site/jacoco/jacoco.xml
    alt SONAR_TOKEN disponível
        GH->>SC: sonar-maven-plugin:sonar (lê o jacoco.xml)
        SC-->>GH: resultado da análise
    else sem token (PR de fork)
        GH->>GH: pula o Sonar
    end
```

Por que cada escolha:

- **`fetch-depth: 0`** — o Sonar precisa do histórico para atribuir código novo × antigo.
- **`./mvnw`** — usa a versão do Maven fixada no repositório (`.mvn/`), igual para todo mundo.
- **Testcontainers** — os testes de banco usam PostgreSQL real, **sem H2** ([ADR 007](../adr/007-testcontainers-postgres.md)).
  O `ubuntu-latest` já traz Docker, então não há configuração extra.
- **Sonar condicional** — PRs vindos de fork não recebem secrets; sem a condição o job falharia.
- **`concurrency` com `cancel-in-progress`** — um push novo cancela a execução anterior da mesma ref,
  economizando minutos.

## `deploy.yml` em detalhe

Disparo: **Actions → Deploy - Projeto Resgate API → Run workflow**. O campo `ref` (padrão `main`) permite
escolher outra branch, tag ou SHA.

O deploy tem **três jobs encadeados**:

```mermaid
flowchart LR
    R["resolve<br/>ref → SHA"] --> C["ci<br/>ci.yml reutilizado<br/>testes + Sonar"]
    C --> B["build-and-push<br/>environment: production<br/>(aprovação)"]
```

| Job              | O que faz                                                                                                   |
|------------------|-------------------------------------------------------------------------------------------------------------|
| `resolve`        | Faz o checkout da `ref` e grava o **SHA exato** (`git rev-parse HEAD`) e a tag `prod-<12 primeiros do SHA>`. O `ref` digitado entra por variável de ambiente, nunca interpolado no script (evita injeção de comando) |
| `ci`             | Chama o `ci.yml` como workflow reutilizável (`workflow_call`) **nesse SHA**: `mvnw clean verify` e Sonar. **Sempre roda** |
| `build-and-push` | Só roda se o `ci` passou (`needs: [resolve, ci]`). Usa `environment: production`, então a **aprovação manual só é pedida para um commit que já passou nos testes** |

Etapas do `build-and-push`:

| Etapa                  | Detalhe                                                                                        |
|------------------------|------------------------------------------------------------------------------------------------|
| Checkout               | No SHA resolvido (o mesmo que o CI validou).                                                   |
| Login                  | `DOCKERHUB_USERNAME` + `DOCKERHUB_TOKEN` (token de acesso, não a senha).                       |
| Build                  | Usa o `Dockerfile` (multi-stage, ver abaixo) com cache `type=gha`.                             |
| Push                   | Duas tags: **`:prod-<sha>`** (imutável, serve de rollback) e **`:latest`** (o que o Watchtower observa). |
| Rastreabilidade        | Labels OCI `org.opencontainers.image.revision` e `version`: `docker inspect` mostra de qual commit saiu a imagem. |

### Limpeza das tags no Docker Hub (manual)

Cada deploy cria uma tag `prod-<sha>` nova, e o pipeline **não apaga nenhuma**. O plano gratuito do Docker Hub tem
armazenamento limitado, então as tags antigas precisam ser **excluídas à mão** de tempos em tempos.

> Houve uma tentativa de limpar automaticamente no fim do `deploy.yml` (pela API do Docker Hub), mas ela não
> funcionou e foi removida.

**Quando:** a cada poucos deploys, ou quando o Docker Hub avisar de espaço.

**Como:**

1. Entre em [hub.docker.com](https://hub.docker.com) com a conta do projeto e abra o repositório da imagem
   (`DOCKER_IMAGE_REPO`).
2. Aba **Tags**: ordene pela data do push (mais novas primeiro).
3. **Mantenha** sempre:
   - `latest`;
   - a tag `prod-` do deploy atual (a mais recente);
   - **pelo menos uma** `prod-` anterior, para rollback imediato.
4. Marque as demais tags `prod-` e use **Delete**. Não apague tags com outro nome.

A tag em uso na VM pode ser conferida com `docker inspect -f '{{.Config.Image}}' projetoresgate_api` e o label
`org.opencontainers.image.version` da imagem. **Nunca apague a tag que está fixada no compose durante um rollback.**

> Antes, a tag usava o `GITHUB_SHA`, que é o commit do workflow e não o da `ref` escolhida. Isso foi corrigido:
> agora a tag, o CI e o build usam o **mesmo SHA**.

### Para que serve o `workflow_call` do `ci.yml`

O `ci.yml` reage a `push`, `pull_request`, manual **e** `workflow_call`. Com o `workflow_call`, o `deploy.yml` o
chama como uma função, passando `inputs.ref` com o SHA a validar. No `push` e no PR o `inputs.ref` é vazio e o
checkout usa o commit do próprio evento. Sem isso, seria possível publicar uma imagem de um commit sem teste.

### O `Dockerfile`

```mermaid
flowchart LR
    A["Stage build<br/>maven:3.9.12-temurin-21"] -->|"dependency:go-offline<br/>(camada cacheada)"| B["mvn package -DskipTests"]
    B -->|"jarmode=tools extract --layers<br/>jar renomeado app.jar"| C["Stage final<br/>eclipse-temurin:21-jre-alpine"]
    C --> D["USER app<br/>java -jar app.jar<br/>porta 8080"]
```

- **Dependências antes do código:** o `pom.xml` é copiado e as dependências baixadas antes do `src`, com cache do
  `.m2`, então mudar só o código reaproveita a camada de dependências.
- **Jar em camadas:** dependências, loader, snapshots e aplicação em camadas separadas. No deploy normalmente só a
  camada de código (centenas de kB) é enviada e baixada, não a imagem inteira.
- **Só JRE e usuário sem privilégios:** a imagem final tem apenas o JRE e roda como o usuário `app` (uid 100),
  não como root. Tamanho da imagem: cerca de 285 MB.
- **Nome fixo `app.jar`:** mudar a versão no `pom.xml` não quebra mais o `ENTRYPOINT`.
- **Memória:** `JAVA_TOOL_OPTIONS="-XX:MaxRAMPercentage=70 -XX:+ExitOnOutOfMemoryError"`. Sem memória, o processo
  morre e o Docker reinicia. Em produção o compose usa `-XX:MaxRAMPercentage=60`.
- **`HEALTHCHECK`:** consulta `/actuator/health/liveness` (início de 90 s para o Flyway e o Spring subirem).
- **Desligamento gracioso:** `server.shutdown: graceful`, com até 25 s por fase. O compose dá `stop_grace_period: 40s`.
- **`.dockerignore`:** mantém `target/`, `.git`, `.env`, docs e testes fora do contexto de build.

Testado com o Spring Boot 4.1.2: build, subida a partir de banco vazio (18 migrations), container `healthy`,
processo como `app` e desligamento gracioso.

## Configuração necessária no GitHub

### Secrets (`Settings → Secrets and variables → Actions`)

| Secret               | Usado em    | Para quê                                                       |
|----------------------|-------------|----------------------------------------------------------------|
| `SONAR_TOKEN`        | `ci.yml`    | Token do SonarCloud                                            |
| `DOCKERHUB_USERNAME` | `deploy.yml`| Usuário do Docker Hub                                          |
| `DOCKERHUB_TOKEN`    | `deploy.yml`| **Access token** do Docker Hub com permissão **Read & Write** (a exclusão de tags antigas é feita à mão, no site) |

> Prefira guardar os dois do Docker Hub no **environment `production`** em vez do nível do repositório.

### Variables

| Variable             | Exemplo                       | Para quê                                 |
|----------------------|-------------------------------|------------------------------------------|
| `DOCKER_IMAGE_REPO`  | `seuusuario/projetoresgate-api` | Nome da imagem no Docker Hub           |
| `SONAR_PROJECT_KEY`  | `org_projetoresgate-api`      | Chave do projeto no SonarCloud           |
| `SONAR_ORGANIZATION` | `org`                         | Organização no SonarCloud                |

Os segredos da **aplicação** (banco, JWT, SMTP) **não** ficam no GitHub: ficam só no `.env` da VM.

> **Sonar falhando com `You must define the following mandatory properties: sonar.projectKey, sonar.organization`:**
> as variáveis `SONAR_PROJECT_KEY` e `SONAR_ORGANIZATION` não foram cadastradas (a aba **Variables**, não Secrets).
> O passo do Sonar roda sempre que o `SONAR_TOKEN` existe e usa essas duas variáveis.

## Segurança do pipeline

| Medida                                                             | Por quê                                                                                     |
|--------------------------------------------------------------------|---------------------------------------------------------------------------------------------|
| GitHub **não tem** credenciais da VM                               | Comprometer o repositório não dá acesso ao servidor nem ao banco.                           |
| Deploy só manual                                                   | Nada vai para produção sem uma pessoa decidir.                                              |
| Environment `production` com **Required reviewers**                | Um segundo olhar (ou o seu clique) antes de publicar. Configure em `Settings → Environments`. |
| Environment restrito à branch `main` (**Deployment branches**)     | Impede publicar e ler secrets a partir de uma branch arbitrária.                            |
| Proteção da `main` (PR obrigatório + CI verde)                     | Só código revisado e testado chega ao que é publicado por padrão.                           |
| Token do Docker Hub com escopo mínimo                              | Vazamento não expõe a conta inteira.                                                        |
| Nunca usar `pull_request_target` com secrets                       | Executaria código de PR de fork com acesso aos secrets.                                     |

## Dependabot

Atualizações automáticas de dependências (Maven, GitHub Actions, Docker e Compose) via PRs semanais: ver [`dependabot.md`](dependabot.md).

## Como fazer um deploy

1. **Actions → Deploy → Run workflow** (deixe `ref = main`). O CI roda sozinho dentro do deploy.
2. Se o CI passar, aprove o ambiente `production` quando o GitHub pedir.
3. Aguarde o job terminar (≈ 3–6 min).
4. Aguarde até **5 minutos** (intervalo do Watchtower) e valide — ver [`runbook.md`](runbook.md#validar-um-deploy).

## Como fazer rollback

A VM roda `:latest`. Para voltar à versão anterior, aponte o compose para a tag `prod-<sha>` anterior —
passo a passo em [`runbook.md`](runbook.md#rollback). O rollback alcança as tags `prod-` que ainda existem no Docker
Hub (as antigas são [excluídas à mão](#limpeza-das-tags-no-docker-hub-manual)); para voltar a uma versão cuja tag já
foi apagada, gere a imagem de novo rodando o `deploy.yml` com a `ref` (commit ou tag do git) desejada.

## Actions fixadas por SHA

Todas as actions de terceiros e do GitHub são referenciadas pelo **SHA completo do commit**, com a versão no
comentário (`uses: docker/login-action@<sha> # v3.7.0`). Uma tag como `v3` pode ser movida para outro código;
o SHA não. O Dependabot (`github-actions`) atualiza o SHA e o comentário juntos. Para trocar uma versão à mão,
pegue o SHA da tag com `git ls-remote --tags https://github.com/<dono>/<action>.git` (use a linha `^{}` quando
existir, que é o commit da tag anotada).

## Evoluções possíveis

| Ideia                                                  | Ganho                                            |
|--------------------------------------------------------|--------------------------------------------------|
| Quality Gate do Sonar bloqueando o merge               | Impede regressão de qualidade/cobertura          |
| Notificação (e-mail/Discord) ao fim do deploy          | Feedback — o job termina antes da VM atualizar   |
