# Documentação de negócio — Projeto Resgate

Esta pasta descreve **o que a ONG faz e o que o sistema precisa fazer**, em linguagem de negócio. Não há
código, endpoint ou nome de classe aqui. Quem quiser a explicação técnica vai para
[`../tecnico/`](../tecnico/mapa-de-modulos.md).

O conteúdo vem dos documentos de levantamento feitos com a equipe da ONG (ver [Origem das informações](#origem-das-informações)). Quando algo
é **decisão tomada**, **proposta** ou **ainda a definir** (❓), isso está indicado.

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
| Significado dos termos                                       | [`glossario.md`](glossario.md)                                |

## Legenda de status

Usada nas tabelas de regras e de funcionalidades:

| Marca | Significado                                                                    |
|-------|--------------------------------------------------------------------------------|
| ✅    | **Funciona hoje** no sistema                                                    |
| 🟡    | **Parcial**: existe parte, falta parte                                          |
| ⬜    | **Pedido pela ONG, ainda não feito**                                            |
| ❓    | **A definir**: depende de uma decisão ou de uma informação da ONG              |

## Origem das informações

Os documentos são resultado de:

- **Reunião de 29/10:** cálculos, processo de cadastro de aluno, campos necessários, telas do Access e anotações
  da equipe pedagógica (entradas, saídas, processamentos, formato da lista de presença);
- **Reunião sobre pessoa física e grupo familiar:** telas de cadastro e listagem;
- **Documento de Mapeamento v1.0 (julho/2025):** processo anual, papéis, ativos tecnológicos e dados;
- **Proposta de Melhoria:** sistema único, perfis de acesso, indicadores e previsões;
- **Documentos de cálculo da ONG:** renda líquida per capita, IHE, distribuição proporcional e lógica da vaquinha.

> Os arquivos originais ficam **fora deste repositório**. Se uma informação aqui divergir deles, **os originais e a
> ONG têm razão**: corrija o documento.
> Quando a ONG decidir algo novo, registre aqui no mesmo dia: documentação desatualizada gera sistema errado.

## Como manter

- Mudou um processo da ONG? Atualize [`processo-anual.md`](processo-anual.md) e as regras afetadas.
- Uma decisão da ONG chegou? Registre-a na regra correspondente e troque ❓ pelo status real.
- Uma funcionalidade ficou pronta? Troque ⬜/🟡 por ✅ em [`situacao-do-sistema.md`](situacao-do-sistema.md).
