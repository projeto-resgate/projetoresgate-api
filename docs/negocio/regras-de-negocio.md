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
| RN-CAD-08 | CPF, RG, CNPJ, CEP e telefones podem ser digitados **com ou sem pontos e traços**: o sistema guarda só os números (e as letras do RG). O RG pode ser o próprio CPF (nova carteira de identidade). | ✅ |
| RN-CAD-09 | Duas pessoas **não podem ter o mesmo RG**, mesmo que digitado com pontuação diferente.                        | ✅ |

## Famílias

| Código   | Regra                                                                                                          | Status |
|----------|----------------------------------------------------------------------------------------------------------------|--------|
| RN-FAM-01 | Cada aluno **pertence a um grupo familiar**; o aluno e **todos os seus responsáveis** precisam ser membros desse grupo. | ✅ |
| RN-FAM-02 | Cada grupo familiar tem **nome único** e um **número amigável** (FAM-1, FAM-2…).                              | ✅ |
| RN-FAM-03 | Renda, rendas per capita e despesas **não podem ser negativas**; moradores **≥ 1**.                           | ✅ |
| RN-FAM-04 | Uma pessoa pode ser vinculada a uma família **existente** ou criada já dentro dela.                           | ✅ |
| RN-FAM-06 | A **renda líquida per capita** é calculada automaticamente: `(renda − educação − habitação − saúde) ÷ moradores`, despesa vazia = 0. | ⬜ (hoje é digitada) |
| RN-FAM-07 | O grupo familiar **não tem endereço**; o endereço do aluno vem da pessoa física.                              | ❓ (hoje é o contrário) |
| RN-FAM-08 | Na ficha da pessoa e na do aluno, aparecem os dados do **grupo familiar**.                                    | 🟡 |
| RN-FAM-09 | Para colocar uma pessoa já cadastrada numa família, **pesquisa-se por CPF, RG, nome ou apelido**, seleciona-se a pessoa e adiciona-se à família. | ✅ |

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
| RN-ALU-10 | O aluno guarda **documentos e termos assinados** (compromisso e autorização de imagem) no **drive interno**.   | ⬜ |
| RN-ALU-11 | Registrar **escolaridade e renda (e sua origem)** de cada responsável.                                        | 🟡 (escolaridade sim; renda por responsável não) |

## Desligamento e retorno

| Código   | Regra                                                                                                          | Status |
|----------|----------------------------------------------------------------------------------------------------------------|--------|
| RN-DES-01 | O aluno está **ativo** ou **desistente**. Na lista de alunos, o ativo aparece como **cursando** (tem turma em andamento hoje) ou **sem turma** (ativo, mas sem turma em andamento hoje). | ✅ |
| RN-DES-02 | Para registrar desistência, o **motivo é obrigatório**; a data é a informada (não pode ser futura) ou a de hoje. | ✅ |
| RN-DES-03 | Não se pode registrar desistência de quem **já desistiu**, nem reativar quem **não está desistente**.         | ✅ |
| RN-DES-04 | Cada desistência e retorno **fica registrado no histórico** do aluno, com motivo, data e hora do registro; a consulta traz o mais recente primeiro. | ✅ (a tela ainda precisa exibir) |
| RN-DES-05 | Ao desistir, o aluno é **retirado de todas as turmas** em que estava (os vínculos ficam inativos).           | ✅ |
| RN-DES-06 | Ao **voltar**, o aluno **não volta automaticamente** às turmas; a menos que a secretaria indique **uma turma** no momento do retorno, e então ele volta **só àquela turma**. | ✅ |
| RN-DES-07 | Um aluno que **já está ativo** pode ser colocado em **outra turma** a qualquer momento, sem aviso de "aluno já ativo". | ✅ |
| RN-DES-08 | O cadastro de um aluno **não é apagado**: ele só pode **desistir** e **voltar**. O histórico fica sempre guardado.   | ✅ |

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
| RN-MAT-04 | Aluno que **contribui com a vaquinha** recebe todos os materiais; quem **não contribui** recebe só os que o projeto **já tem**. | ⬜ (falta definir como o sistema sabe do pagamento) |
| RN-MAT-05 | A retirada do **vale-transporte** exige **assinatura**, com **lista por turma**.                              | ⬜ |
| RN-MAT-07 | Aluno que **não participa das formações perde o direito à renovação** dos materiais no ano seguinte, **mesmo recadastrando**. | ⬜ |

## Benefícios: IHE, vaquinha e vale-transporte

Detalhes e exemplos em [`beneficios-e-calculos.md`](beneficios-e-calculos.md).

| Código   | Regra                                                                                                          | Status |
|----------|----------------------------------------------------------------------------------------------------------------|--------|
| RN-BEN-01 | O **IHE** de cada família é `1 − (0,5 × RLT_normalizada + 0,5 × RLPC_normalizada)`, com normalização **Min–Max** sobre a base. | ⬜ |
| RN-BEN-02 | IHE vai de **0** (menos vulnerável) a **1** (mais vulnerável).                                                 | ⬜ |
| RN-BEN-03 | O **valor total da vaquinha** é um **parâmetro geral** informado pela Administração.                           | ⬜ |
| RN-BEN-04 | O **desconto da vaquinha** depende da faixa de 20% do ranking de carência: **100%, 80%, 60%, 20% e 0%** (do mais ao menos carente). | ⬜ |
| RN-BEN-05 | Valor da vaquinha da pessoa = valor total × (1 − desconto da faixa).                                           | ⬜ |
| RN-BEN-06 | Mudar o valor da vaquinha vale **só para novas matrículas e para quem ainda não teve o boleto gerado**.       | ⬜ |
| RN-BEN-07 | Cada pessoa tem a marcação **"boleto da vaquinha gerado"** (sim/não).                                          | ⬜ |
| RN-BEN-08 | O **valor total do transporte** é um **parâmetro geral**.                                                      | ⬜ |
| RN-BEN-09 | O vale-transporte de cada pessoa = **total × (IHE da pessoa ÷ soma dos IHE)**; a soma é exatamente o total.   | ⬜ |
| RN-BEN-10 | Quem tem **IHE 0 recebe 0**; se todos tiverem o mesmo IHE, **todos recebem igual**.                            | ⬜ |

## Cadastros apagados

Quando alguém apaga um cadastro (uma escola, um programa, uma pessoa, uma série), o que **já foi registrado**
com ele não some nem fica sem sentido.

| Código   | Regra                                                                                                          | Status |
|----------|----------------------------------------------------------------------------------------------------------------|--------|
| RN-EXC-01 | Quem já usava um cadastro apagado **continua aparecendo**, com o **nome** do cadastro apagado (ex.: a turma continua mostrando o programa que foi apagado). | ✅ |
| RN-EXC-02 | Um registro que já existe **continua podendo ser alterado**, mesmo que o cadastro apagado que ele usa continue ligado a ele. | ✅ |
| RN-EXC-03 | **Não é possível usar** um cadastro apagado em um **novo** registro (ex.: criar uma turma de um programa apagado, ou colocar em turma um aluno cuja pessoa foi apagada). | ✅ |
| RN-EXC-04 | Cadastros apagados **não aparecem** nas buscas de seleção (ex.: ao escolher um aluno ou um educador).           | ✅ |
| RN-EXC-05 | Apagar uma **série escolar** não muda o nome das turmas que já a usavam.                                       | ✅ |
| RN-EXC-06 | Apagar uma pessoa ou um educador **não tira** a pessoa da lista de alunos/educadores da turma.                   | ✅ |
| RN-EXC-07 | Apagar um **grupo familiar** não impede de alterar os alunos daquela família.                                    | ✅ |
| RN-EXC-08 | O CPF, RG ou CNPJ de um cadastro apagado **pode ser cadastrado de novo**.                                       | ✅ |
| RN-EXC-09 | Não é possível tirar de uma família o **aluno** (se for a única família dele) nem um **responsável** por aluno daquela família. | ✅ |
| RN-EXC-10 | Não é possível remover de um programa uma **categoria de educador** que está em uso em alguma turma.             | ✅ |

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
