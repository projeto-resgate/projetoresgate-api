# Perguntas em aberto

O que **ainda precisa ser decidido ou esclarecido com a ONG** antes de construir ou ajustar. Cada pergunta tem
um código (`PA-NN`). Quando for respondida, **mova a decisão** para a regra correspondente em
[`regras-de-negocio.md`](regras-de-negocio.md) e **apague a pergunta daqui**.

Prioridade: 🔴 bloqueia a próxima etapa · 🟠 importante · 🟢 pode esperar.

## Divergências entre o que foi decidido e o que o sistema faz

| Código | Pergunta | Prio | Contexto |
|--------|----------|------|----------|
| PA-01 | **Endereço:** o grupo familiar tem endereço ou o endereço fica na pessoa física? | 🟠 | Na reunião ficou decidido que a **família não tem endereço** e o do aluno vem da **pessoa física**. O sistema hoje guarda o endereço **na família** e a pessoa não tem endereço. Uma pessoa mora em um só lugar; e se uma pessoa está em duas famílias (pais separados)? |
| PA-02 | **Uma pessoa pode estar em mais de uma família?** | 🟠 | Hoje pode. Pais separados e crianças que alternam de casa tornam isso real, mas afeta o cálculo de renda e moradores. |
| PA-03 | Como **achar a família pelo CPF de um membro**: se a pessoa estiver em duas famílias, qual aparece? | 🟢 | Decisão da reunião; ainda não implementada. |

## Cálculos e benefícios

| Código | Pergunta | Prio |
|--------|----------|------|
| PA-04 | Qual a **fórmula do índice de carência** (pesos de renda, moradores e de cada gasto)? Qual o **nome oficial**? | 🔴 |
| PA-05 | Quais os **limites e significados de cada faixa** (baixíssima … muito alta)? | 🔴 |
| PA-06 | Quem **recebe vale-transporte** (qual faixa) e **de quanto**? | 🔴 |
| PA-07 | Como se calculam o **valor da vaquinha** e o **desconto**? | 🟠 |
| PA-08 | O índice é **recalculado** a cada recadastro? Precisamos guardar o **histórico** (para o indicador de evolução)? | 🟠 |
| PA-09 | Na renda do responsável, entram **salário, pensão, autônomos**? Registramos **por pessoa** ou só o total da família? | 🟠 |

## Alunos, formações e equipes

| Código | Pergunta | Prio |
|--------|----------|------|
| PA-10 | O que é **APE**? Como "bolsista de APE" se diferencia das condições de bolsa que o sistema tem hoje (sem bolsa, integral, parcial, assistência social)? | 🟠 |
| PA-11 | O que é uma **equipe** dentro da formação? Tem nome, voluntário próprio, horário próprio? | 🟠 |
| PA-12 | **"Formação" é o mesmo que "turma"?** E o **programa** é o tipo de formação (ex.: "Formação de Valores")? | 🟠 |
| PA-13 | O **turno** do aluno é dado próprio ou deriva do horário da formação? | 🟢 |
| PA-14 | Quando o aluno **volta** depois de desistir, ele deve **voltar automaticamente às turmas** que tinha? | 🟠 |
| PA-15 | Pode **matricular na turma** um aluno desistente? E turmas **acima da previsão de alunos**? O sistema deve bloquear ou só avisar? | 🟢 |
| PA-16 | O sistema deve avisar se dois horários da mesma turma **se sobrepõem**? | 🟢 |
| PA-17 | Precisamos do **nome do pai** além do da mãe? | 🟢 |
| PA-18 | **Mentor** é um papel próprio ou é o voluntário? | 🟢 |

## Presença, participação e renovação

| Código | Pergunta | Prio |
|--------|----------|------|
| PA-19 | **Quando o aluno "não participou"?** Qual percentual de presença mínimo para ter direito à renovação? | 🔴 |
| PA-20 | O que conta como **falta recorrente** para disparar alerta (quantas seguidas? em quanto tempo)? | 🟠 |
| PA-21 | A **falta justificada** conta como presença para a renovação? | 🟠 |
| PA-22 | Quem **registra a presença**: o tutor na hora da aula (celular?) ou a secretaria depois? Os tutores têm acesso a internet na aula? | 🔴 |
| PA-23 | Qual o **prazo exato do período de cadastro** (datas de início e fim) e ele muda todo ano? | 🟠 |

## Acesso e dados

| Código | Pergunta | Prio |
|--------|----------|------|
| PA-24 | O que **cada perfil pode ver e fazer**? (ex.: o tutor vê **só as suas turmas**? A secretaria vê a renda das famílias?) | 🔴 |
| PA-25 | **LGPD:** dados de crianças, renda e documentos. Quem autoriza o tratamento? Onde ficam os termos? Por quanto tempo guardamos? | 🟠 |
| PA-26 | **Migração:** podemos receber uma **exportação do Access e do EVN**? Qual é a fonte oficial quando os dois divergem? | 🔴 |
| PA-27 | O que acontece com um aluno que **aparece nas duas bases** com dados diferentes? | 🟠 |
| PA-28 | A **escolaridade e a série** dos responsáveis é pelo mesmo catálogo de séries do aluno? Precisa de "ensino superior", "pós"? | 🟢 |

## Comunicação e relatórios

| Código | Pergunta | Prio |
|--------|----------|------|
| PA-29 | Os **avisos às famílias** são só por e-mail ou também WhatsApp? | 🟠 |
| PA-30 | O **feedback dos pais** continua em Google Forms (com integração) ou passa a ser tela do sistema? | 🟢 |
| PA-31 | Quais **relatórios** são mais urgentes? Quem os recebe (parceiros, universidades, prefeitura)? | 🟠 |
| PA-32 | A integração com o **DoeFácil** precisa trazer doações para dentro do sistema ou só relatórios? | 🟢 |

## Como levantar as respostas

1. Leve esta lista, por **prioridade 🔴**, para uma reunião curta com a secretaria e a diretoria.
2. Para cálculos (PA-04 a PA-08), peça **exemplos reais do Access** (alunos de cada faixa) para validar o
   resultado do sistema contra o que a ONG já usa.
3. Para acesso (PA-24), monte com eles uma **matriz perfil × ação** (quem cria, vê, edita, exclui o quê).
4. Registre cada resposta **com data e quem decidiu**, no próprio documento de regras.
