# Documentação

Índice de toda a documentação do `projetoresgate-api`. Comece por
[`AGENTS.md`](../AGENTS.md) na raiz se você for uma IA ou alguém entrando no projeto.

## Ordem de leitura sugerida

| Arquivo | Quando abrir |
| --- | --- |
| [`AGENTS.md`](../AGENTS.md) | Sempre, antes de codar. Regras invioláveis e comandos de build/teste. |
| [`.opencode/skills/`](../.opencode/skills/) | Quando a tarefa é uma das quatro: feature, endpoint, migration, teste. Carregada sob demanda. |
| [`architecture.md`](architecture.md) | Para saber onde uma classe nova deve morar e o que pode importar o quê. |
| [`convencoes.md`](convencoes.md) | Para copiar o formato de uma entidade, repository, service ou controller. |
| [`testes.md`](testes.md) | Para escrever ou consertar um teste. |
| [`banco-de-dados.md`](banco-de-dados.md) | Para criar migration, índice, sequence ou subir o ambiente local. |
| [`adr/README.md`](adr/README.md) | Para achar a decisão de uma linha que rege o que você está fazendo. |
| [`rfc/README.md`](rfc/README.md) | Antes de propor algo que ainda não é decisão. |

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
