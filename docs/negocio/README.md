# Documentação de negócio — Projeto Resgate

Esta pasta descreve **o que a ONG faz e o que o sistema precisa fazer**, em linguagem de negócio. Não há
código, endpoint ou nome de classe aqui. Quem quiser a explicação técnica vai para
[`../tecnico/`](../tecnico/mapa-de-modulos.md).

O conteúdo vem dos documentos de levantamento feitos com a equipe da ONG (ver [Fontes](#fontes)). Quando algo
é **decisão tomada**, **proposta** ou **dúvida**, isso está indicado.

## Por onde começar

| Quero saber…                                                 | Documento                                                     |
|--------------------------------------------------------------|---------------------------------------------------------------|
| Quem é o Projeto Resgate e quem faz o quê                    | [`o-projeto.md`](o-projeto.md)                                |
| Como funciona o ano, do cadastro à renovação                 | [`processo-anual.md`](processo-anual.md)                      |
| Quais cadastros existem e quais dados guardam                | [`cadastros.md`](cadastros.md)                                |
| Como se calcula a ajuda a cada aluno (carência, vale, vaquinha) | [`beneficios-e-calculos.md`](beneficios-e-calculos.md)     |
| Todas as regras, em linguagem simples                        | [`regras-de-negocio.md`](regras-de-negocio.md)                |
| Quais relatórios e indicadores a ONG quer                    | [`indicadores-e-relatorios.md`](indicadores-e-relatorios.md)  |
| **O que o sistema já faz e o que falta**                     | [`situacao-do-sistema.md`](situacao-do-sistema.md)            |
| O que ainda precisa ser decidido com a ONG                   | [`perguntas-em-aberto.md`](perguntas-em-aberto.md)            |
| Significado dos termos                                       | [`glossario.md`](glossario.md)                                |

## Legenda de status

Usada nas tabelas de regras e de funcionalidades:

| Marca | Significado                                                                    |
|-------|--------------------------------------------------------------------------------|
| ✅    | **Funciona hoje** no sistema                                                    |
| 🟡    | **Parcial**: existe parte, falta parte                                          |
| ⬜    | **Pedido pela ONG, ainda não feito**                                            |
| ❓    | **A definir**: depende de uma decisão ou de uma informação da ONG              |

## Fontes

| Fonte                                                                                   | O que contém                                                                                       |
|-----------------------------------------------------------------------------------------|----------------------------------------------------------------------------------------------------|
| [`../drive/dadosgerais.md`](../drive/dadosgerais.md)                                    | Resumo de reunião (telas de pessoa física e grupo familiar), **Documento de Mapeamento v1.0 (julho/2025)** e **Proposta de Melhoria** |
| [`../drive/Resumo Reunião 29_10.pdf`](../drive/Resumo%20Reuni%C3%A3o%2029_10.pdf)       | Resumo da reunião de 29/10: cálculos, processo de cadastro de aluno, campos, prints do Access e anotações manuscritas da equipe pedagógica (entradas, saídas, processamentos, lista de presença) |

> Se uma informação aqui divergir das fontes, **as fontes e a ONG têm razão**. Corrija o documento.
> Quando a ONG decidir algo novo, registre aqui no mesmo dia: documentação desatualizada gera sistema errado.

## Como manter

- Mudou um processo da ONG? Atualize [`processo-anual.md`](processo-anual.md) e as regras afetadas.
- Uma dúvida foi respondida? Tire de [`perguntas-em-aberto.md`](perguntas-em-aberto.md) e coloque a decisão na regra correspondente.
- Uma funcionalidade ficou pronta? Troque ⬜/🟡 por ✅ em [`situacao-do-sistema.md`](situacao-do-sistema.md).
