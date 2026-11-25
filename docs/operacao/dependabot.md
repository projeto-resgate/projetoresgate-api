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
   `@dependabot ignore this major version`, `@dependabot ignore this dependency`. Veja
   [Recusar uma atualização](#recusar-uma-atualização) antes de usar os de *ignore*.

## Recusar uma atualização

Há três jeitos de dizer "não" a um PR do Dependabot. Eles têm alcances bem diferentes.

| Como                                               | O que acontece                                                                                       | Quando usar |
|----------------------------------------------------|------------------------------------------------------------------------------------------------------|-------------|
| **Só fechar o PR**                                 | Ele não reabre **aquela** versão. Quando sair uma versão mais nova, abre um PR novo                  | Recusa pontual: "esta versão, não" |
| Comentar `@dependabot ignore this major version` e fechar | Ele para de sugerir **qualquer versão daquela major** (ex.: todo o Java 24). Correções da versão atual continuam chegando | A major inteira não serve, mas a próxima pode servir |
| Comentar `@dependabot ignore this dependency` e fechar | Ele **nunca mais** sugere nada daquela dependência                                                   | Raramente. Você perde também as correções de segurança |

Também existe `@dependabot ignore this minor version`, que vale para uma versão *minor* inteira (ex.: todo o 3.9.x).

### Como o comando funciona

- É um **comentário** no PR (aba *Conversation*), com o texto exato `@dependabot ignore this major version`. O
  Dependabot responde no próprio PR confirmando e fecha o PR sozinho.
- A "major" é sempre a do **número de versão que o Dependabot lê na dependência**, não a de algo que vem dentro dela.
  Exemplo: em `eclipse-temurin:21-jre-alpine-3.24` a versão lida é `21` (o Java); em
  `maven:3.9.12-eclipse-temurin-21` a versão lida é `3.9.12` (o **Maven**), e o `21` é só parte da tag.
- A regra fica guardada **no GitHub, não no repositório**: não aparece no `dependabot.yml` nem passa por code review.
  Para conferir o que está ignorado, comente em qualquer PR do Dependabot
  `@dependabot show <nome da dependência> ignore conditions`.
- Para desfazer: `@dependabot unignore this major version` (ou `unignore this dependency`) num PR daquela
  dependência.
- Se a regra deve valer para sempre e ficar documentada, prefira o `ignore` no próprio
  [`.github/dependabot.yml`](../../.github/dependabot.yml), como já é feito para o Spring Boot e o Postgres.

### Exemplo: imagens Java do Docker

O projeto roda em **Java 21 (LTS)**. O Dependabot sugere subir as imagens para versões do Java que **não são LTS**
(24, 26), com suporte curto. Essas trocas devem ser recusadas:

| PR sugerido                                                        | O que fazer | Por quê |
|--------------------------------------------------------------------|-------------|---------|
| `eclipse-temurin` `21-jre-alpine-…` → `24-jre-alpine-…`            | Comentar `@dependabot ignore this major version` e fechar | A versão lida é a do Java: o comando ignora o Java 24 inteiro e mantém as correções do 21 |
| `maven` `3.9.12-eclipse-temurin-21` → `3.9-eclipse-temurin-26`     | **Só fechar**, sem comando | A versão lida é a do Maven (3). O comando ignoraria **todo o Maven 3**, inclusive as correções |

Quando o projeto decidir trocar de Java (por exemplo, para a próxima LTS), a mudança é feita à mão, nos dois `FROM`
do `Dockerfile` e no `java.version` do `pom.xml`, como uma migração planejada.

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
