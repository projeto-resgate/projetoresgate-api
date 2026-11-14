# Regras de negócio

As regras do Projeto Resgate em linguagem simples, com **código estável** (`RN-…`) para citar em conversas,
tarefas e testes. Para onde cada regra está no código, veja
[`../tecnico/regras-implementadas.md`](../tecnico/regras-implementadas.md).

Status: ✅ funciona hoje · 🟡 parcial · ⬜ pedido, ainda não feito · ❓ a definir
(ver [legenda](README.md#legenda-de-status)).

## Cadastro e período

| Código   | Regra                                                                                                          | Status |
|----------|----------------------------------------------------------------------------------------------------------------|--------|
| RN-CAD-01 | O cadastro de alunos acontece **entre setembro e novembro**.                                                  | ⬜ |
| RN-CAD-02 | Cadastro **fora do período** é permitido, mas o aluno **não tem garantia** de receber os materiais.           | ⬜ |
| RN-CAD-03 | A **secretaria** faz o cadastro e o recadastro anual dos alunos.                                              | 🟡 (não há perfil de secretaria) |
| RN-CAD-04 | Todo dado pessoal fica **em um único cadastro de pessoa** (sem repetir CPF, nome etc. por papel).             | ✅ |
| RN-CAD-05 | Duas pessoas **não podem ter o mesmo CPF**; duas empresas **não podem ter o mesmo CNPJ**.                     | ✅ |
| RN-CAD-06 | Data de nascimento **não pode ser futura**.                                                                   | ✅ |
| RN-CAD-07 | Telefone, celular, e-mail, gênero e nascimento são coletados de **todas** as pessoas.                         | ✅ |

## Famílias

| Código   | Regra                                                                                                          | Status |
|----------|----------------------------------------------------------------------------------------------------------------|--------|
| RN-FAM-01 | Cada aluno **pertence a um grupo familiar**; o aluno e **todos os seus responsáveis** precisam ser membros desse grupo. | ✅ |
| RN-FAM-02 | Cada grupo familiar tem **nome único** e um **número amigável** (FAM-1, FAM-2…).                              | ✅ |
| RN-FAM-03 | Renda, rendas per capita e despesas **não podem ser negativas**; moradores **≥ 1**.                           | ✅ |
| RN-FAM-04 | Uma pessoa pode ser vinculada a uma família **existente** ou criada já dentro dela.                           | ✅ |
| RN-FAM-05 | Para vincular alguém a uma família existente, basta informar o **CPF de qualquer membro**.                    | ⬜ |
| RN-FAM-06 | A **renda líquida per capita** é calculada automaticamente (renda ÷ moradores).                               | ⬜ (hoje é digitada) |
| RN-FAM-07 | O grupo familiar **não tem endereço**; o endereço do aluno vem da pessoa física.                              | ❓ (hoje é o contrário) |
| RN-FAM-08 | Na ficha da pessoa e na do aluno, aparecem os dados do **grupo familiar**.                                    | 🟡 |

## Alunos e responsáveis

| Código   | Regra                                                                                                          | Status |
|----------|----------------------------------------------------------------------------------------------------------------|--------|
| RN-ALU-01 | Para ser aluno, a pessoa precisa **já estar cadastrada** como pessoa física.                                  | ✅ |
| RN-ALU-02 | Uma pessoa **só pode ser aluno uma vez**; para novas formações, ganha **nova matrícula** (não novo cadastro). | ✅ |
| RN-ALU-03 | **Pais e responsáveis podem ser alunos** (há formações para eles).                                            | ✅ |
| RN-ALU-04 | Um aluno **não pode ser responsável de si mesmo**, nem ter a mesma pessoa duas vezes como responsável.        | ✅ |
| RN-ALU-05 | Cada responsável tem **parentesco** e **função** (comunicação e/ou financeiro).                               | ✅ |
| RN-ALU-06 | O nome do **pai e da mãe** é registrado independente de serem responsáveis.                                   | 🟡 (só a mãe) |
| RN-ALU-07 | O aluno informa **escola, série, matrícula escolar, ano letivo** e **condição de bolsa**.                     | ✅ |
| RN-ALU-08 | Deve ser possível **diferenciar aluno bolsista de APE**.                                                      | ❓ |
| RN-ALU-09 | O ano letivo precisa ser um ano válido (entre 1900 e 2100).                                                   | ✅ |
| RN-ALU-10 | O aluno guarda **documentos e termos assinados** (compromisso e autorização de imagem).                       | ⬜ |
| RN-ALU-11 | Registrar **escolaridade e renda (e sua origem)** de cada responsável.                                        | 🟡 (escolaridade sim; renda por responsável não) |

## Desligamento e retorno

| Código   | Regra                                                                                                          | Status |
|----------|----------------------------------------------------------------------------------------------------------------|--------|
| RN-DES-01 | O aluno está **ativo** ou **desistente**.                                                                    | ✅ |
| RN-DES-02 | Para registrar desistência, o **motivo é obrigatório**; a data é a informada ou a de hoje.                    | ✅ |
| RN-DES-03 | Não se pode registrar desistência de quem **já desistiu**, nem reativar quem **não está desistente**.         | ✅ |
| RN-DES-04 | Cada desistência e retorno **fica registrado no histórico** do aluno, com data.                              | 🟡 (grava, mas ainda não aparece nas telas) |
| RN-DES-05 | Ao desistir, o aluno é **retirado de todas as turmas** em que estava (os vínculos ficam inativos).           | ✅ |
| RN-DES-06 | Ao **voltar**, o aluno **não volta automaticamente** às turmas; a secretaria precisa matriculá-lo de novo.    | ✅ (a confirmar se é o desejado) |

## Formações (turmas)

| Código   | Regra                                                                                                          | Status |
|----------|----------------------------------------------------------------------------------------------------------------|--------|
| RN-FOR-01 | Toda formação tem **programa**, **instituição**, **previsão de alunos**, **início** e **duração**.            | ✅ |
| RN-FOR-02 | O **término** é calculado: início + duração em meses.                                                         | ✅ |
| RN-FOR-03 | Cada horário tem **dia da semana**, **início** e **fim**, e o fim vem **depois** do início.                   | ✅ |
| RN-FOR-04 | O **nome da turma é automático**: `programa - séries - dias`.                                                 | ✅ |
| RN-FOR-05 | Um aluno **não pode estar duas vezes** na mesma turma; pode estar em **várias** turmas.                       | ✅ |
| RN-FOR-06 | Um voluntário **não pode estar duas vezes** na mesma turma; sua **função** deve ser uma das **definidas no programa**. | ✅ |
| RN-FOR-07 | A formação tem uma **fase escolar sugerida**, mas a **alocação final** considera a **disponibilidade dos voluntários**. | 🟡 (sugestão existe; sem disponibilidade) |
| RN-FOR-08 | O aluno informa **seus períodos disponíveis** e a equipe o aloca numa formação compatível.                    | ⬜ |
| RN-FOR-09 | Uma formação pode ter **várias equipes**; o aluno é alocado em **uma** delas.                                 | ❓ |
| RN-FOR-10 | O sistema **não impede** matricular alunos além da previsão, nem horários sobrepostos (a previsão é estimativa).| ✅ (comportamento atual) |

## Presença

| Código   | Regra                                                                                                          | Status |
|----------|----------------------------------------------------------------------------------------------------------------|--------|
| RN-PRE-01 | O **tutor** registra a presença em **cada aula**.                                                            | ⬜ |
| RN-PRE-02 | Cada presença pode ter **comentário/justificativa** (falta justificada).                                      | ⬜ |
| RN-PRE-03 | O sistema **alerta** sobre **faltas recorrentes**.                                                            | ⬜ |
| RN-PRE-04 | A frequência calcula o **percentual de participação** do aluno.                                               | ⬜ |

## Materiais e benefícios

| Código   | Regra                                                                                                          | Status |
|----------|----------------------------------------------------------------------------------------------------------------|--------|
| RN-MAT-01 | A **lista de materiais** de cada aluno depende da **série** dele.                                            | ⬜ |
| RN-MAT-02 | Antes de comprar, confere-se o **estoque**; o que falta é **orçado** e pedido às editoras parceiras.         | ⬜ |
| RN-MAT-03 | **Livros** são retirados pelo aluno na unidade; **apostilas** são entregues na **escola**.                   | ⬜ |
| RN-MAT-04 | Aluno que **contribui com a vaquinha** recebe todos os materiais; quem **não contribui** recebe só os que o projeto **já tem**. | ⬜ |
| RN-MAT-05 | **Vale-transporte** só para **baixa renda**, definido pelo índice de carência; a retirada exige **assinatura**.| ⬜ |
| RN-MAT-06 | O nível de ajuda de cada aluno vem do **índice de carência** (renda, moradores, gastos).                      | ⬜ |
| RN-MAT-07 | Aluno que **não participa das formações perde o direito à renovação** dos materiais no ano seguinte, **mesmo recadastrando**. | ⬜ |

## Acesso

| Código   | Regra                                                                                                          | Status |
|----------|----------------------------------------------------------------------------------------------------------------|--------|
| RN-ACE-01 | Existem três níveis: **Administração** (tudo), **Secretaria** (cadastros, presença, materiais, comunicação) e **Professores/Voluntários** (suas turmas, presença, feedback). | 🟡 (só Administração, sem restrições) |
| RN-ACE-02 | Só quem está autenticado acessa os dados.                                                                    | ✅ |

## Comunicação

| Código   | Regra                                                                                                          | Status |
|----------|----------------------------------------------------------------------------------------------------------------|--------|
| RN-COM-01 | A secretaria **repassa avisos às famílias**; o sistema deve enviar **avisos por e-mail**.                    | ⬜ |
| RN-COM-02 | O **feedback dos responsáveis** (ao fim de cada semestre) deve ficar ligado ao aluno.                         | ⬜ |
| RN-COM-03 | Painel de **pendências**: faltas, feedbacks não enviados, materiais a entregar.                               | ⬜ |
