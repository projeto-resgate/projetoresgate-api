# Dependabot

O Dependabot é um robô do GitHub que **abre Pull Requests** para atualizar as dependências do projeto. Ele não
altera a `main` sozinho: cada atualização chega como um PR, o CI roda nele, e **uma pessoa decide** se faz o merge.
A configuração está em [`.github/dependabot.yml`](../../.github/dependabot.yml).

## O que ele vigia

| Ecossistema        | Onde olha                                              | Agrupamento                                  | Prefixo do commit   | Labels                   |
|--------------------|--------------------------------------------------------|----------------------------------------------|---------------------|--------------------------|
| `maven`            | `pom.xml`                                              | **Um PR único** com todas as atualizações *minor* e *patch* | `chore(deps)`       | `dependencies`, `java`   |
| `github-actions`   | `.github/workflows/*.yml` (versões das actions)         | Um PR único com todas                         | `chore(ci)`         | `dependencies`, `ci`     |
| `docker`           | `Dockerfile` (imagens base)                            | Um PR por imagem                              | `chore(docker)`     | `dependencies`, `docker` |
| `docker-compose`   | `docker-compose.yml` e `deploy/docker-compose.prod.yml`| Um PR por imagem                              | `chore(docker)`     | `dependencies`, `docker` |

- **Quando:** toda **segunda-feira, 06:00** (horário de São Paulo).
- **Limite:** no máximo 5 PRs abertos do Maven por vez.
- **Por que agrupar:** *minor* e *patch* chegam juntos em um PR só, então são poucos PRs por semana. Versões *major*
  (que costumam quebrar compatibilidade) chegam **separadas**, para você avaliar uma a uma.

## O que ele ignora de propósito

| Ignorado                                                   | Por quê                                                                                           |
|------------------------------------------------------------|---------------------------------------------------------------------------------------------------|
| **Major** do `spring-boot-starter-parent`                  | Trocar de versão maior do Spring Boot (3 → 4, por exemplo) muda pacotes, starters e testes. É uma migração planejada, feita à mão. *Minor* e *patch* do Boot continuam chegando |
| **Major** da imagem `postgres` no compose                  | Subir a versão maior do Postgres exige exportar e importar os dados (não basta trocar a imagem). Ver [RFC 0002](../rfc/0002-alinhar-versao-postgres.md) |

## Como tratar um PR do Dependabot

1. **Espere o CI.** Ele roda `mvnw clean verify` e o Sonar no PR, como em qualquer outro.
2. **CI verde, atualização minor/patch:** pode fazer o merge. Leia as notas de versão do PR se for uma biblioteca
   importante (Spring, Hibernate, JWT).
3. **CI vermelho:** abra o log. Pode ser uma incompatibilidade real. Corrija no próprio PR (você pode dar commit na
   branch dele) ou feche o PR.
4. **Major:** trate como tarefa. Leia o guia de migração, rode a aplicação localmente e só então faça o merge.
5. **Comandos úteis, dentro do PR**, como comentário: `@dependabot rebase` (reaplica sobre a `main`),
   `@dependabot ignore this major version`, `@dependabot ignore this dependency`.

> O merge na `main` **não faz deploy**. O deploy continua manual (ver [`pipeline-ci-cd.md`](pipeline-ci-cd.md)).
> Depois de aceitar atualizações, rode o deploy quando quiser publicar.

## Para ligar

1. **Settings → Code security** do repositório: ative **Dependabot alerts** e **Dependabot security updates**
   (alertas e correções automáticas de vulnerabilidade).
2. O arquivo `dependabot.yml` na `main` ativa as atualizações de versão (as semanais acima). Não precisa de mais nada.
3. Crie os labels `dependencies`, `java`, `ci` e `docker` em **Issues → Labels** se ainda não existirem, para os PRs
   saírem etiquetados.

## Dependabot e o Docker Hub

O Dependabot atualiza as **imagens base** usadas pelo projeto (`maven`, `eclipse-temurin`, `postgres`,
`watchtower`). Ele **não** mexe na imagem da sua aplicação no Docker Hub. Essa continua sendo publicada pelo
`deploy.yml`.
