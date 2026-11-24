# Documentação

Índice de toda a documentação do `projetoresgate-api`. Comece por
[`AGENTS.md`](../AGENTS.md) na raiz se você for uma IA ou alguém entrando no projeto.

## Ordem de leitura sugerida

| Arquivo                                     | Quando abrir                                                                                  |
|---------------------------------------------|-----------------------------------------------------------------------------------------------|
| [`AGENTS.md`](../AGENTS.md)                 | Sempre, antes de codar. Regras invioláveis e comandos de build/teste.                         |
| [`.opencode/skills/`](../.opencode/skills/) | Quando a tarefa é uma das quatro: feature, endpoint, migration, teste. Carregada sob demanda. |
| [`architecture.md`](architecture.md)        | Para saber onde uma classe nova deve morar e o que pode importar o quê.                       |
| [`convencoes.md`](convencoes.md)            | Para copiar o formato de uma entidade, repository, service ou controller.                     |
| [`testes.md`](testes.md)                    | Para escrever ou consertar um teste.                                                          |
| [`banco-de-dados.md`](banco-de-dados.md)    | Para criar migration, índice, sequence ou subir o ambiente local.                             |
| [`adr/README.md`](adr/README.md)            | Para achar a decisão de uma linha que rege o que você está fazendo.                           |
| [`rfc/README.md`](rfc/README.md)            | Antes de propor algo que ainda não é decisão.                                                 |

## Documentação de negócio (para a ONG e para quem decide)

Em linguagem de negócio, sem código. Comece por [`negocio/README.md`](negocio/README.md).

| Arquivo                                                                 | Quando abrir                                                    |
|-------------------------------------------------------------------------|-----------------------------------------------------------------|
| [`negocio/o-projeto.md`](negocio/o-projeto.md)                          | Entender a ONG, os papéis e como a informação é guardada hoje.  |
| [`negocio/processo-anual.md`](negocio/processo-anual.md)                | Ver o ciclo do ano: cadastro, materiais, formações, renovação.  |
| [`negocio/cadastros.md`](negocio/cadastros.md)                          | Saber quais dados cada cadastro guarda.                         |
| [`negocio/beneficios-e-calculos.md`](negocio/beneficios-e-calculos.md)  | Fórmulas do IHE, vaquinha e vale-transporte.                    |
| [`negocio/regras-de-negocio.md`](negocio/regras-de-negocio.md)          | Consultar uma regra (códigos `RN-…`) e seu status.              |
| [`negocio/indicadores-e-relatorios.md`](negocio/indicadores-e-relatorios.md) | O que a ONG quer extrair do sistema.                       |
| [`negocio/situacao-do-sistema.md`](negocio/situacao-do-sistema.md)      | **O que já existe × o que falta** e ordem sugerida.             |
| [`negocio/glossario.md`](negocio/glossario.md)                          | Significado dos termos.                                         |

## Documentação técnica de funcionamento

Como o sistema está implementado hoje (para quem desenvolve).

| Arquivo                                                        | Quando abrir                                                     |
|----------------------------------------------------------------|------------------------------------------------------------------|
| [`tecnico/mapa-de-modulos.md`](tecnico/mapa-de-modulos.md)     | Ver os módulos e como se ligam.                                  |
| [`tecnico/modelo-de-dados.md`](tecnico/modelo-de-dados.md)     | Diagramas ER e estados.                                          |
| [`tecnico/diagrama-de-classes.md`](tecnico/diagrama-de-classes.md) | Classes do domínio, enums e relações (JPA × por UUID).        |
| [`tecnico/autenticacao.md`](tecnico/autenticacao.md)           | Fluxos de login, refresh, logout, reset de senha.                |
| [`tecnico/fluxo-educandos.md`](tecnico/fluxo-educandos.md)     | Passo a passo técnico de aluno, responsável e família.           |
| [`tecnico/fluxo-turmas.md`](tecnico/fluxo-turmas.md)           | Passo a passo técnico de turma, horário e vínculos.              |
| [`tecnico/regras-implementadas.md`](tecnico/regras-implementadas.md) | Cada regra e **onde está no código** (HTTP, classe).       |
| [`tecnico/endpoints.md`](tecnico/endpoints.md)                 | Mapa das rotas da API.                                           |

## Operação (pipeline, deploy e produção)

| Arquivo                                                              | Quando abrir                                                   |
|----------------------------------------------------------------------|----------------------------------------------------------------|
| [`operacao/pipeline-ci-cd.md`](operacao/pipeline-ci-cd.md)           | Entender o CI (Sonar) e o deploy manual para o Docker Hub.     |
| [`operacao/infraestrutura-deploy.md`](operacao/infraestrutura-deploy.md) | Montar a VM, variáveis, Watchtower, backup.                |
| [`operacao/runbook.md`](operacao/runbook.md)                         | Validar deploy, ver logs, voltar versão, diagnosticar.         |
| [`operacao/dependabot.md`](operacao/dependabot.md)                   | Como funcionam os PRs automáticos de atualização.              |
| [`operacao/frontend-deploy.md`](operacao/frontend-deploy.md)         | Publicar o front (Docker Hub, nginx, Caddy, HTTPS) e ligá-lo à API. |

## ADRs e RFCs

- **Índice semântico dos ADRs:** [`adr/README.md`](adr/README.md) — uma linha por decisão, com o
  que manda na prática. É o ponto de partida para achar a regra certa sem varrer os arquivos.
- **RFCs:** [`rfc/README.md`](rfc/README.md) — propostas ainda não decididas. Uma RFC é escrita
  **antes** de implementar; depois de aceita e implementada, vira ADR.

Não há tabela de ADRs aqui de propósito: o índice existe em um lugar só, e esse lugar é o
`adr/README.md`.

## Sobre este repositório de documentação

Documentação desatualizada é pior do que documentação ausente: ela faz um agente de IA escrever
código errado com confiança. Se você encontrar um documento que contradiz o código, o certo é
corrigir o documento na hora, no mesmo PR da mudança.
