# RFCs — Request for Comments

Uma RFC é uma **proposta** em discussão, escrita antes de implementar. É o oposto do ADR: o ADR
registra o que **foi decidido**; a RFC registra o que está **em aberto**.

## ADR ou RFC?

|                        | ADR                         | RFC                            |
|------------------------|-----------------------------|--------------------------------|
| Momento                | Depois de decidir           | Antes de decidir               |
| Conteúdo               | Decisão e consequências     | Problema, opções, recomendação |
| Muda depois de aceito? | Não, marca-se como superado | Sim, enquanto não aceita       |
| Seguir como regra?     | Sim                         | **Não**                        |

**Não trate uma RFC como regra ao escrever código.** Se a sua tarefa depende do que a RFC propõe,
é escopo novo e precisa de alinhamento antes.

## Fluxo

1. **Numere** sequencialmente: `docs/rfc/0001-nome-curto.md`.
2. Escreva com o template abaixo, preenchendo a seção de opções de verdade. Uma RFC com uma
   única opção não é uma RFC.
3. Discuta. A RFC muda enquanto não for aceita.
4. Quando aceita, implemente e escreva o ADR correspondente, referenciando a RFC.
5. Marque a RFC como `Aceita` e aponte para o ADR.

## Status

- `Proposta` — redigida, em discussão.
- `Em revisão` — Comments incorporados, aguardando aprovação.
- `Aceita` — aprovada, ADR escrito ou em escrita.
- `Rejeitada` — não vai ser feita. Fica no repositório, com o motivo.
- `Substituída` — outra RFC a substitui.

## Propostas abertas

| RFC                                     | Título                                          | Status   |
|-----------------------------------------|-------------------------------------------------|----------|
| [0001](0001-autorizacao-por-role.md)    | Autorização por role                            | Proposta |
| [0002](0002-alinhar-versao-postgres.md) | Alinhar versão do PostgreSQL entre dev e testes | Proposta |

## Template

Copie este arquivo para `docs/rfc/XXXX-nome.md`.

```markdown
# NNN. Título da proposta

Data: AAAA-MM-DD
Status: Proposta
Autor: <quem>

## Problema

O que está errado ou ausente, em uma ou duas frases. Cite o ADR ou o código que promete algo que
não existe, se for o caso.

## Contexto

Fatos relevantes: o que existe hoje, o que restringe a solução, o que já foi tentado.

## Opções

### Opção A — <nome>

Como funciona. Prós e contras. Custo.

### Opção B — <nome>

Como funciona. Prós e contras. Custo.

## Recomendação

Qual opção, e por quê. Seja específico sobre por que as outras foram descartadas.

## Impacto

- **Código:** o que muda.
- **Banco:** se precisa de migration.
- **API:** se muda contrato.
- **Testes:** o que precisa de teste novo.
- **Docs:** quais ADRs precisam de atualização.

## Decisão

Preenchido depois da discussão: `Aceita: opção X`, com link para o ADR resultante.
```
