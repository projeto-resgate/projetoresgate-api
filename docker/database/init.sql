DO $$
BEGIN
    ------------------------------------------------------------------
    -- users e papéis de acesso
    ------------------------------------------------------------------
    INSERT INTO users (id, email, password, name, nickname, token_version, date_created, date_updated) VALUES
        ('00000000-0000-0000-0000-000000000001', 'admin@projetoresgate.com', '$2b$10$hCY2pM6q1FgBXil.4SGUOOQC1sdRqBsW5qSEtdXdLh0VqWN9DzNiu', 'Administrador', 'admin', 0, now(), now()),
        ('00000000-0000-0000-0000-000000000002', 'maria.silva@example.com', '$2b$10$kF0WoXQ.fZbm.LXkXTMVt.H7.cuNDVOKziUfJmqqHAK8DtafYXrcG', 'Maria Silva', 'maria', 0, now(), now()),
        ('00000000-0000-0000-0000-000000000003', 'contato@projetoresgate.com.br', '$2b$10$kF0WoXQ.fZbm.LXkXTMVt.H7.cuNDVOKziUfJmqqHAK8DtafYXrcG', 'Contato Projeto Resgate', 'projeto', 0, now(), now())
    ON CONFLICT DO NOTHING;

    INSERT INTO user_roles (user_id, role)
    SELECT u.id, v.role
    FROM users u
    JOIN (VALUES
        ('00000000-0000-0000-0000-000000000001'::uuid, 'ADMIN'),
        ('00000000-0000-0000-0000-000000000002'::uuid, 'ADMIN'),
        ('00000000-0000-0000-0000-000000000003'::uuid, 'ADMIN')
    ) AS v(user_id, role) ON v.user_id = u.id
    WHERE NOT EXISTS (
        SELECT 1 FROM user_roles ur WHERE ur.user_id = u.id AND ur.role = v.role
    );

    ------------------------------------------------------------------
    -- Pessoas físicas: 3 usuários do seed + as famílias Silva,
    -- Pereira, Souza e Ramos (pais, mães, irmãos e educandos)
    ------------------------------------------------------------------
    INSERT INTO natural_person (id, name, email, nickname, cpf, rg, birth_date, gender, phone, cellphone, is_email_verified, date_created, date_updated) VALUES
        ('00000000-0000-0000-0000-000000000011', 'Maria Silva', 'maria.silva@example.com', 'maria', '52998224725', '123456789', '1995-04-12', 'FEMALE', '1130001111', '11911112222', TRUE, now(), now()),
        ('00000000-0000-0000-0000-000000000012', 'João Pereira', 'joao.pereira@example.com', 'joaop', '11144477735', '234567890', '1990-08-03', 'MALE', '1130002222', '11933334444', TRUE, now(), now()),
        ('00000000-0000-0000-0000-000000000013', 'Ana Souza', 'ana.souza@example.com', 'ana', '39053344705', '345678901', '2000-01-25', 'FEMALE', '3130003333', '31955556666', TRUE, now(), now()),
        ('00000000-0000-0000-0000-000000000014', 'Pedro Silva', 'pedro.silva@example.com', 'pedros', '12345678909', '445566778', '2015-06-18', 'MALE', '1130004444', '11944445555', TRUE, now(), now()),
        ('00000000-0000-0000-0000-000000000015', 'Júlia Pereira', 'julia.pereira@example.com', 'juliap', '98765432100', '556677889', '2019-03-09', 'FEMALE', '1130005555', '11955556666', TRUE, now(), now()),
        ('00000000-0000-0000-0000-000000000016', 'Carlos Souza', 'carlos.souza@example.com', 'carloss', '24681357928', '667788990', '2011-11-27', 'MALE', '3130006666', '31966667777', TRUE, now(), now()),
        ('00000000-0000-0000-0000-000000000017', 'Rafael Silva', 'rafael.silva@example.com', 'rafaels', '10293847541', '778899001', '1988-07-05', 'MALE', '1130007777', '11977778888', TRUE, now(), now()),
        ('00000000-0000-0000-0000-000000000018', 'Paula Pereira', 'paula.pereira@example.com', 'paulap', '56789012303', '889900112', '1989-12-02', 'FEMALE', '1130008888', '11988889999', TRUE, now(), now()),
        ('00000000-0000-0000-0000-000000000019', 'Camila Souza', 'camila.souza@example.com', 'camilas', '71524386928', '990011223', '2009-09-15', 'FEMALE', '3130009999', '31999990000', TRUE, now(), now()),
        ('00000000-0000-0000-0000-000000000020', 'Diego Ramos', 'diego.ramos@example.com', 'diegor', '31415926590', '101112134', '1987-05-20', 'MALE', '1930001111', '19911112222', TRUE, now(), now()),
        ('00000000-0000-0000-0000-000000000021', 'Sofia Ramos', 'sofia.ramos@example.com', 'sofias', '20246813520', '111213145', '2014-08-30', 'FEMALE', '1930002222', '19922223333', TRUE, now(), now()),
        ('00000000-0000-0000-0000-000000000022', 'Helena Ramos', 'helena.ramos@example.com', 'helenar', '65432109824', '121314156', '1986-10-11', 'FEMALE', '1930003333', '19933334444', TRUE, now(), now())
    ON CONFLICT DO NOTHING;

    ------------------------------------------------------------------
    -- Endereços
    ------------------------------------------------------------------
    INSERT INTO address (id, street_type, street_name, zip_code, number, reference_point, complement, neighborhood, city, state, date_created, date_updated) VALUES
        ('00000000-0000-0000-0000-000000000021', 'Rua', 'das Palmeiras', '01001000', '100', 'Próximo à praça', 'Sala 1', 'Centro', 'São Paulo', 'SP', now(), now()),
        ('00000000-0000-0000-0000-000000000022', 'Avenida', 'Paulista', '01310100', '205', 'Em frente ao museu', 'Andar 3', 'Bela Vista', 'São Paulo', 'SP', now(), now()),
        ('00000000-0000-0000-0000-000000000023', 'Rua', 'do Rosário', '30130010', '400', NULL, NULL, 'Centro', 'Belo Horizonte', 'MG', now(), now()),
        ('00000000-0000-0000-0000-000000000024', 'Rua', 'dos Lírios', '13010100', '78', 'Próximo ao parque', 'Bloco B', 'Jardim América', 'Campinas', 'SP', now(), now()),
        ('00000000-0000-0000-0000-000000000025', 'Avenida', 'Brasil', '20040002', '1500', NULL, 'Loja 2', 'Centro', 'Rio de Janeiro', 'RJ', now(), now())
    ON CONFLICT DO NOTHING;

    ------------------------------------------------------------------
    -- Pessoas jurídicas (instituições)
    ------------------------------------------------------------------
    INSERT INTO legal_person (id, cnpj, corporate_name, trade_name, display_name, main_cnae_code, registration_status, company_status, address_id, representative_name, representative_cellphone, representative_phone, representative_email, date_created, date_updated) VALUES
        ('00000000-0000-0000-0000-000000000031', '11222333000181', 'Instituto Projeto Resgate', 'Projeto Resgate', 'Projeto Resgate', '8550301', 'ACTIVE', 'ACTIVE', '00000000-0000-0000-0000-000000000021', 'Carlos Andrade', '11977778888', '1133334444', 'carlos@projetoresgate.com.br', now(), now()),
        ('00000000-0000-0000-0000-000000000032', '11444777000161', 'Fundação Amanhã', 'Fundação Amanhã', 'Fundação Amanhã', '8550301', 'ACTIVE', 'ACTIVE', '00000000-0000-0000-0000-000000000022', 'Fernanda Lima', '11966665555', '1134445555', 'fernanda@fundacaoamanha.org', now(), now()),
        ('00000000-0000-0000-0000-000000000033', '12345678000195', 'Associação Educar Mais', 'Educar Mais', 'Educar Mais', '8550301', 'ACTIVE', 'ACTIVE', '00000000-0000-0000-0000-000000000023', 'Paulo Ramos', '31955554444', '3132223333', 'paulo@educarmais.org', now(), now()),
        ('00000000-0000-0000-0000-000000000034', '45997418000153', 'Instituto Comunitário Ponte Viva', 'Ponte Viva', 'Ponte Viva', '8550301', 'ACTIVE', 'ACTIVE', '00000000-0000-0000-0000-000000000025', 'Renata Costa', '21987654321', '2133334444', 'renata@ponteviva.org', now(), now())
    ON CONFLICT DO NOTHING;

    ------------------------------------------------------------------
    -- Programas e itens de categoria do educador
    ------------------------------------------------------------------
    INSERT INTO program (id, name, web_site_url, status, institution_id, date_created, date_updated) VALUES
        ('00000000-0000-0000-0000-000000000041', 'Programa Resgate Jovem', 'https://resgatejovem.org', 'ACTIVE', '00000000-0000-0000-0000-000000000031', now(), now()),
        ('00000000-0000-0000-0000-000000000042', 'Programa Cidadania Digital', 'https://cidadaniadigital.org', 'ACTIVE', '00000000-0000-0000-0000-000000000031', now(), now()),
        ('00000000-0000-0000-0000-000000000043', 'Programa Primeiro Emprego', 'https://primeiroemprego.org', 'INACTIVE', '00000000-0000-0000-0000-000000000031', now(), now()),
        ('00000000-0000-0000-0000-000000000044', 'Programa Amanhã na Escola', 'https://amanhaescola.org', 'ACTIVE', '00000000-0000-0000-0000-000000000032', now(), now()),
        ('00000000-0000-0000-0000-000000000045', 'Programa Educar para Vencer', 'https://educarparavencer.org', 'ACTIVE', '00000000-0000-0000-0000-000000000033', now(), now())
    ON CONFLICT DO NOTHING;

    INSERT INTO educator_category_item (id, name, program_id, date_created, date_updated) VALUES
        ('00000000-0000-0000-0000-000000000051', 'Orientação Profissional', '00000000-0000-0000-0000-000000000041', now(), now()),
        ('00000000-0000-0000-0000-000000000052', 'Reforço Escolar', '00000000-0000-0000-0000-000000000041', now(), now()),
        ('00000000-0000-0000-0000-000000000053', 'Esporte e Cultura', '00000000-0000-0000-0000-000000000041', now(), now()),
        ('00000000-0000-0000-0000-000000000054', 'Inclusão Digital', '00000000-0000-0000-0000-000000000042', now(), now()),
        ('00000000-0000-0000-0000-000000000055', 'Informática Básica', '00000000-0000-0000-0000-000000000042', now(), now()),
        ('00000000-0000-0000-0000-000000000056', 'Currículo e Entrevista', '00000000-0000-0000-0000-000000000043', now(), now()),
        ('00000000-0000-0000-0000-000000000057', 'Capacitação Profissional', '00000000-0000-0000-0000-000000000043', now(), now()),
        ('00000000-0000-0000-0000-000000000058', 'Orientação de Estudos', '00000000-0000-0000-0000-000000000044', now(), now()),
        ('00000000-0000-0000-0000-000000000059', 'Bolsa de Material Escolar', '00000000-0000-0000-0000-000000000044', now(), now()),
        ('00000000-0000-0000-0000-000000000060', 'Monitoria de Matemática', '00000000-0000-0000-0000-000000000045', now(), now()),
        ('00000000-0000-0000-0000-000000000061', 'Oficina de Leitura', '00000000-0000-0000-0000-000000000045', now(), now())
    ON CONFLICT DO NOTHING;

    ------------------------------------------------------------------
    -- Grupos familiares e membros
    -- Cada grupo reúne os pais/irmãos das pessoas que viram educando
    -- nos blocos de learner e learner_responsible abaixo.
    ------------------------------------------------------------------
    INSERT INTO family_group (id, friendly_id, name, household_income, per_capita_income, education_expense, health_expense, housing_expense, number_of_residents, address_id, date_created, date_updated) VALUES
        ('00000000-0000-0000-0000-000000000061', 'FAM-1', 'Família Silva', 5000.00, 1250.00, 800.00, 400.00, 1500.00, 4, '00000000-0000-0000-0000-000000000021', now(), now()),
        ('00000000-0000-0000-0000-000000000062', 'FAM-2', 'Família Pereira', 3200.00, 800.00, 600.00, 300.00, 1200.00, 4, '00000000-0000-0000-0000-000000000022', now(), now()),
        ('00000000-0000-0000-0000-000000000063', 'FAM-3', 'Família Souza', 1800.00, 600.00, 250.00, 150.00, 900.00, 3, '00000000-0000-0000-0000-000000000023', now(), now()),
        ('00000000-0000-0000-0000-000000000064', 'FAM-4', 'Família Ramos', 4100.00, 1025.00, 500.00, 250.00, 1100.00, 4, '00000000-0000-0000-0000-000000000024', now(), now())
    ON CONFLICT DO NOTHING;

    INSERT INTO family_group_natural_person (family_group_id, natural_person_id) VALUES
        ('00000000-0000-0000-0000-000000000061', '00000000-0000-0000-0000-000000000011'),
        ('00000000-0000-0000-0000-000000000061', '00000000-0000-0000-0000-000000000014'),
        ('00000000-0000-0000-0000-000000000061', '00000000-0000-0000-0000-000000000017'),
        ('00000000-0000-0000-0000-000000000062', '00000000-0000-0000-0000-000000000012'),
        ('00000000-0000-0000-0000-000000000062', '00000000-0000-0000-0000-000000000015'),
        ('00000000-0000-0000-0000-000000000062', '00000000-0000-0000-0000-000000000018'),
        ('00000000-0000-0000-0000-000000000063', '00000000-0000-0000-0000-000000000013'),
        ('00000000-0000-0000-0000-000000000063', '00000000-0000-0000-0000-000000000016'),
        ('00000000-0000-0000-0000-000000000063', '00000000-0000-0000-0000-000000000019'),
        ('00000000-0000-0000-0000-000000000064', '00000000-0000-0000-0000-000000000020'),
        ('00000000-0000-0000-0000-000000000064', '00000000-0000-0000-0000-000000000021'),
        ('00000000-0000-0000-0000-000000000064', '00000000-0000-0000-0000-000000000022')
    ON CONFLICT DO NOTHING;

    -- A sequence acompanha o maior friendlyId já existente (seed ou cadastro via API),
    -- para que o próximo grupo local não repita um número já usado.
    PERFORM setval('family_group_friendly_id_seq',
        GREATEST(
            (SELECT COALESCE(MAX((substring(friendly_id FROM '^FAM-([0-9]+)$'))::bigint), 0) FROM family_group),
            (SELECT last_value FROM family_group_friendly_id_seq)
        ),
        true
    );

    ------------------------------------------------------------------
    -- Séries escolares
    -- A abreviação é o que aparece no título da turma (ver docs/turmas-e-colaboradores.md).
    -- Quando em branco, o título cai no nome completo da série.
    ------------------------------------------------------------------
    INSERT INTO school_grade (id, name, abbreviation, grade_order, date_created, date_updated) VALUES
        ('00000000-0000-0000-0000-000000000071', 'Primeiro ano do ensino fundamental', '1º ano', 1, now(), now()),
        ('00000000-0000-0000-0000-000000000072', 'Segundo ano do ensino fundamental', '2º ano', 2, now(), now()),
        ('00000000-0000-0000-0000-000000000073', 'Terceiro ano do ensino fundamental', '3º ano', 3, now(), now()),
        ('00000000-0000-0000-0000-000000000074', 'Quarto ano do ensino fundamental', '4º ano', 4, now(), now()),
        ('00000000-0000-0000-0000-000000000075', 'Quinto ano do ensino fundamental', '5º ano', 5, now(), now()),
        ('00000000-0000-0000-0000-000000000076', 'Sexto ano do ensino fundamental', '6º ano', 6, now(), now()),
        ('00000000-0000-0000-0000-000000000077', 'Sétimo ano do ensino fundamental', '7º ano', 7, now(), now()),
        ('00000000-0000-0000-0000-000000000078', 'Oitavo ano do ensino fundamental', '8º ano', 8, now(), now()),
        ('00000000-0000-0000-0000-000000000079', 'Nono ano do ensino fundamental', '9º ano', 9, now(), now()),
        ('00000000-0000-0000-0000-000000000080', 'Primeiro ano do ensino médio', '1º EM', 10, now(), now()),
        ('00000000-0000-0000-0000-000000000081', 'Segundo ano do ensino médio', '2º EM', 11, now(), now()),
        ('00000000-0000-0000-0000-000000000082', 'Terceiro ano do ensino médio', '3º EM', 12, now(), now())
    ON CONFLICT DO NOTHING;

    -- Preenche a abreviação num banco já semeado antes da coluna existir (re-execução do seeder).
    UPDATE school_grade SET abbreviation = v.abbreviation, date_updated = now()
    FROM (VALUES
        ('00000000-0000-0000-0000-000000000071'::uuid, '1º ano'),
        ('00000000-0000-0000-0000-000000000072'::uuid, '2º ano'),
        ('00000000-0000-0000-0000-000000000073'::uuid, '3º ano'),
        ('00000000-0000-0000-0000-000000000074'::uuid, '4º ano'),
        ('00000000-0000-0000-0000-000000000075'::uuid, '5º ano'),
        ('00000000-0000-0000-0000-000000000076'::uuid, '6º ano'),
        ('00000000-0000-0000-0000-000000000077'::uuid, '7º ano'),
        ('00000000-0000-0000-0000-000000000078'::uuid, '8º ano'),
        ('00000000-0000-0000-0000-000000000079'::uuid, '9º ano'),
        ('00000000-0000-0000-0000-000000000080'::uuid, '1º EM'),
        ('00000000-0000-0000-0000-000000000081'::uuid, '2º EM'),
        ('00000000-0000-0000-0000-000000000082'::uuid, '3º EM')
    ) AS v(id, abbreviation)
    WHERE school_grade.id = v.id AND school_grade.abbreviation IS DISTINCT FROM v.abbreviation;

    ------------------------------------------------------------------
    -- Educandos
    -- Todo educando precisa ser membro de um grupo familiar; a instituição
    -- fica nula quando a vaga ainda não foi definida (Júlia) e a série/ano
    -- letivo ficam nulos no cadastro em aberto (Sofia).
    ------------------------------------------------------------------
    INSERT INTO learner (id, natural_person_id, institution_id, school_grade_id, interest_arts, scholarship_condition, academic_year, mother_name, school_enrollment, date_created, date_updated) VALUES
        ('00000000-0000-0000-0000-000000000091', '00000000-0000-0000-0000-000000000014', '00000000-0000-0000-0000-000000000031', '00000000-0000-0000-0000-000000000076', TRUE, 'PARTIAL', 2026, 'Maria Silva', '2026001', now(), now()),
        ('00000000-0000-0000-0000-000000000092', '00000000-0000-0000-0000-000000000015', NULL, '00000000-0000-0000-0000-000000000072', FALSE, 'NONE', 2026, 'Paula Pereira', '2026002', now(), now()),
        ('00000000-0000-0000-0000-000000000093', '00000000-0000-0000-0000-000000000016', '00000000-0000-0000-0000-000000000033', '00000000-0000-0000-0000-000000000080', TRUE, 'SOCIAL_ASSISTANCE', 2026, 'Ana Souza', '2026003', now(), now()),
        ('00000000-0000-0000-0000-000000000094', '00000000-0000-0000-0000-000000000019', '00000000-0000-0000-0000-000000000033', '00000000-0000-0000-0000-000000000082', FALSE, 'FULL', 2026, 'Ana Souza', '2026004', now(), now()),
        ('00000000-0000-0000-0000-000000000095', '00000000-0000-0000-0000-000000000021', '00000000-0000-0000-0000-000000000034', NULL, FALSE, 'NONE', NULL, 'Helena Ramos', NULL, now(), now())
    ON CONFLICT DO NOTHING;

    ------------------------------------------------------------------
    -- Responsáveis dos educandos
    -- Todos são membros do mesmo grupo familiar do educando. Camila é
    -- educanda (linha 0094) e também responsável do irmão Carlos (0093),
    -- exercendo os dois papéis.
    ------------------------------------------------------------------
    INSERT INTO learner_responsible (id, learner_id, natural_person_id, responsible_type, kinship, school_grade_id, date_created, date_updated) VALUES
        ('00000000-0000-0000-0000-000000000101', '00000000-0000-0000-0000-000000000091', '00000000-0000-0000-0000-000000000011', 'COMMUNICATION', 'MOTHER', '00000000-0000-0000-0000-000000000076', now(), now()),
        ('00000000-0000-0000-0000-000000000102', '00000000-0000-0000-0000-000000000091', '00000000-0000-0000-0000-000000000017', 'FINANCIAL', 'FATHER', NULL, now(), now()),
        ('00000000-0000-0000-0000-000000000103', '00000000-0000-0000-0000-000000000092', '00000000-0000-0000-0000-000000000018', 'COMMUNICATION', 'MOTHER', '00000000-0000-0000-0000-000000000072', now(), now()),
        ('00000000-0000-0000-0000-000000000104', '00000000-0000-0000-0000-000000000092', '00000000-0000-0000-0000-000000000012', 'FINANCIAL', 'FATHER', NULL, now(), now()),
        ('00000000-0000-0000-0000-000000000105', '00000000-0000-0000-0000-000000000093', '00000000-0000-0000-0000-000000000013', 'COMMUNICATION', 'MOTHER', '00000000-0000-0000-0000-000000000080', now(), now()),
        ('00000000-0000-0000-0000-000000000106', '00000000-0000-0000-0000-000000000093', '00000000-0000-0000-0000-000000000019', 'FINANCIAL', 'SISTER', NULL, now(), now()),
        ('00000000-0000-0000-0000-000000000107', '00000000-0000-0000-0000-000000000094', '00000000-0000-0000-0000-000000000013', 'COMMUNICATION', 'MOTHER', '00000000-0000-0000-0000-000000000082', now(), now()),
        ('00000000-0000-0000-0000-000000000108', '00000000-0000-0000-0000-000000000095', '00000000-0000-0000-0000-000000000020', 'FINANCIAL', 'FATHER', NULL, now(), now()),
        ('00000000-0000-0000-0000-000000000109', '00000000-0000-0000-0000-000000000095', '00000000-0000-0000-0000-000000000022', 'COMMUNICATION', 'MOTHER', NULL, now(), now())
    ON CONFLICT DO NOTHING;

    ------------------------------------------------------------------
    -- Papéis das pessoas físicas (LEARNER / RESPONSIBLE)
    -- Reflete o vínculo declarado nos blocos acima, como faria o
    -- LearnerRoleSynchronizer ao salvar um educando pela API.
    ------------------------------------------------------------------
    INSERT INTO natural_person_role (id, natural_person_id, role, date_created, date_updated) VALUES
        ('00000000-0000-0000-0000-000000000111', '00000000-0000-0000-0000-000000000014', 'LEARNER', now(), now()),
        ('00000000-0000-0000-0000-000000000112', '00000000-0000-0000-0000-000000000015', 'LEARNER', now(), now()),
        ('00000000-0000-0000-0000-000000000113', '00000000-0000-0000-0000-000000000016', 'LEARNER', now(), now()),
        ('00000000-0000-0000-0000-000000000114', '00000000-0000-0000-0000-000000000019', 'LEARNER', now(), now()),
        ('00000000-0000-0000-0000-000000000115', '00000000-0000-0000-0000-000000000021', 'LEARNER', now(), now()),
        ('00000000-0000-0000-0000-000000000116', '00000000-0000-0000-0000-000000000011', 'RESPONSIBLE', now(), now()),
        ('00000000-0000-0000-0000-000000000117', '00000000-0000-0000-0000-000000000017', 'RESPONSIBLE', now(), now()),
        ('00000000-0000-0000-0000-000000000118', '00000000-0000-0000-0000-000000000012', 'RESPONSIBLE', now(), now()),
        ('00000000-0000-0000-0000-000000000119', '00000000-0000-0000-0000-000000000018', 'RESPONSIBLE', now(), now()),
        ('00000000-0000-0000-0000-000000000120', '00000000-0000-0000-0000-000000000013', 'RESPONSIBLE', now(), now()),
        ('00000000-0000-0000-0000-000000000121', '00000000-0000-0000-0000-000000000019', 'RESPONSIBLE', now(), now()),
        ('00000000-0000-0000-0000-000000000122', '00000000-0000-0000-0000-000000000020', 'RESPONSIBLE', now(), now()),
        ('00000000-0000-0000-0000-000000000123', '00000000-0000-0000-0000-000000000022', 'RESPONSIBLE', now(), now())
    ON CONFLICT DO NOTHING;

    ------------------------------------------------------------------
    -- Colaboradores (educadores)
    -- Todo colaborador parte de uma pessoa física obrigatória. A pessoa
    -- jurídica é o vínculo empregatício/voluntário, opcional. Diego e
    -- Helena ficam sem vínculo com turma, para exercitar a listagem.
    ------------------------------------------------------------------
    INSERT INTO collaborator (id, is_voluntary, natural_person_id, legal_person_id, date_created, date_updated) VALUES
        ('00000000-0000-0000-0000-000000000201', FALSE, '00000000-0000-0000-0000-000000000011', '00000000-0000-0000-0000-000000000031', now(), now()),
        ('00000000-0000-0000-0000-000000000202', TRUE,  '00000000-0000-0000-0000-000000000012', NULL, now(), now()),
        ('00000000-0000-0000-0000-000000000203', FALSE, '00000000-0000-0000-0000-000000000013', '00000000-0000-0000-0000-000000000033', now(), now()),
        ('00000000-0000-0000-0000-000000000204', TRUE,  '00000000-0000-0000-0000-000000000017', '00000000-0000-0000-0000-000000000031', now(), now()),
        ('00000000-0000-0000-0000-000000000205', FALSE, '00000000-0000-0000-0000-000000000018', '00000000-0000-0000-0000-000000000032', now(), now()),
        ('00000000-0000-0000-0000-000000000206', TRUE,  '00000000-0000-0000-0000-000000000020', NULL, now(), now()),
        ('00000000-0000-0000-0000-000000000207', FALSE, '00000000-0000-0000-0000-000000000022', '00000000-0000-0000-0000-000000000034', now(), now())
    ON CONFLICT DO NOTHING;

    ------------------------------------------------------------------
    -- Turmas
    -- O título é materializado: precisa ser exatamente o que o backend
    -- compõe (programa - séries abreviadas - dias abreviados). Mantenha
    -- em sincronia com os vínculos de série e os horários abaixo.
    ------------------------------------------------------------------
    INSERT INTO class_group (id, title, student_projections, start_date, end_date, note, program_id, institution_id, active, date_created, date_updated) VALUES
        ('00000000-0000-0000-0000-000000000181', 'Programa Resgate Jovem - 6º ano, 7º ano - Seg, Qua', 30, '2026-08-03 08:00:00', '2027-02-03 08:00:00', 'Turma de reforço escolar no contraturno.', '00000000-0000-0000-0000-000000000041', '00000000-0000-0000-0000-000000000031', TRUE, now(), now()),
        ('00000000-0000-0000-0000-000000000182', 'Programa Cidadania Digital - 1º EM - Ter, Qui', 25, '2026-09-01 14:00:00', '2027-01-01 14:00:00', 'Laboratório de informática, duas turmas por semana.', '00000000-0000-0000-0000-000000000042', '00000000-0000-0000-0000-000000000031', TRUE, now(), now()),
        ('00000000-0000-0000-0000-000000000183', 'Programa Educar para Vencer - 3º EM - Sáb', 20, '2026-09-05 09:00:00', '2027-03-05 09:00:00', 'Encontro de sábado para conclusão do ensino médio.', '00000000-0000-0000-0000-000000000045', '00000000-0000-0000-0000-000000000033', TRUE, now(), now()),
        ('00000000-0000-0000-0000-000000000184', 'Programa Amanhã na Escola - 2º ano - Sex', 18, '2026-08-07 13:00:00', '2027-01-07 13:00:00', NULL, '00000000-0000-0000-0000-000000000044', '00000000-0000-0000-0000-000000000032', FALSE, now(), now())
    ON CONFLICT DO NOTHING;

    ------------------------------------------------------------------
    -- Séries sugeridas de cada turma
    ------------------------------------------------------------------
    INSERT INTO class_school_grade (class_group_id, school_grade_id) VALUES
        ('00000000-0000-0000-0000-000000000181', '00000000-0000-0000-0000-000000000076'),
        ('00000000-0000-0000-0000-000000000181', '00000000-0000-0000-0000-000000000077'),
        ('00000000-0000-0000-0000-000000000182', '00000000-0000-0000-0000-000000000080'),
        ('00000000-0000-0000-0000-000000000183', '00000000-0000-0000-0000-000000000082'),
        ('00000000-0000-0000-0000-000000000184', '00000000-0000-0000-0000-000000000072')
    ON CONFLICT DO NOTHING;

    ------------------------------------------------------------------
    -- Grade de horários das turmas
    ------------------------------------------------------------------
    INSERT INTO class_schedule (id, class_group_id, week_day, start_time, end_time, active, date_created, date_updated) VALUES
        ('00000000-0000-0000-0000-000000000191', '00000000-0000-0000-0000-000000000181', 'MONDAY', '08:00:00', '10:00:00', TRUE, now(), now()),
        ('00000000-0000-0000-0000-000000000192', '00000000-0000-0000-0000-000000000181', 'WEDNESDAY', '08:00:00', '10:00:00', TRUE, now(), now()),
        ('00000000-0000-0000-0000-000000000193', '00000000-0000-0000-0000-000000000182', 'TUESDAY', '14:00:00', '16:00:00', TRUE, now(), now()),
        ('00000000-0000-0000-0000-000000000194', '00000000-0000-0000-0000-000000000182', 'THURSDAY', '14:00:00', '16:00:00', TRUE, now(), now()),
        ('00000000-0000-0000-0000-000000000195', '00000000-0000-0000-0000-000000000183', 'SATURDAY', '09:00:00', '12:00:00', TRUE, now(), now()),
        ('00000000-0000-0000-0000-000000000196', '00000000-0000-0000-0000-000000000184', 'FRIDAY', '13:00:00', '15:00:00', TRUE, now(), now())
    ON CONFLICT DO NOTHING;

    ------------------------------------------------------------------
    -- Educadores vinculados às turmas
    -- O item de categoria precisa pertencer ao programa da turma.
    -- O vínculo 0216 fica inativo para exercitar o filtro por situação.
    ------------------------------------------------------------------
    INSERT INTO class_collaborator (id, class_group_id, collaborator_id, educator_category_item_id, active, date_created, date_updated) VALUES
        ('00000000-0000-0000-0000-000000000211', '00000000-0000-0000-0000-000000000181', '00000000-0000-0000-0000-000000000201', '00000000-0000-0000-0000-000000000052', TRUE, now(), now()),
        ('00000000-0000-0000-0000-000000000212', '00000000-0000-0000-0000-000000000181', '00000000-0000-0000-0000-000000000204', '00000000-0000-0000-0000-000000000051', TRUE, now(), now()),
        ('00000000-0000-0000-0000-000000000213', '00000000-0000-0000-0000-000000000182', '00000000-0000-0000-0000-000000000203', '00000000-0000-0000-0000-000000000054', TRUE, now(), now()),
        ('00000000-0000-0000-0000-000000000214', '00000000-0000-0000-0000-000000000183', '00000000-0000-0000-0000-000000000202', '00000000-0000-0000-0000-000000000060', TRUE, now(), now()),
        ('00000000-0000-0000-0000-000000000215', '00000000-0000-0000-0000-000000000184', '00000000-0000-0000-0000-000000000205', '00000000-0000-0000-0000-000000000058', TRUE, now(), now()),
        ('00000000-0000-0000-0000-000000000216', '00000000-0000-0000-0000-000000000182', '00000000-0000-0000-0000-000000000205', '00000000-0000-0000-0000-000000000055', FALSE, now(), now())
    ON CONFLICT DO NOTHING;

    ------------------------------------------------------------------
    -- Educandos vinculados às turmas
    -- Cada educando entra na turma da sua série atual. Sofia (0095)
    -- fica sem turma porque ainda não tem série/ano letivo definido.
    ------------------------------------------------------------------
    INSERT INTO class_learner (id, class_group_id, learner_id, active, date_created, date_updated) VALUES
        ('00000000-0000-0000-0000-000000000221', '00000000-0000-0000-0000-000000000181', '00000000-0000-0000-0000-000000000091', TRUE, now(), now()),
        ('00000000-0000-0000-0000-000000000222', '00000000-0000-0000-0000-000000000182', '00000000-0000-0000-0000-000000000093', TRUE, now(), now()),
        ('00000000-0000-0000-0000-000000000223', '00000000-0000-0000-0000-000000000183', '00000000-0000-0000-0000-000000000094', TRUE, now(), now()),
        ('00000000-0000-0000-0000-000000000224', '00000000-0000-0000-0000-000000000184', '00000000-0000-0000-0000-000000000092', TRUE, now(), now())
    ON CONFLICT DO NOTHING;

    INSERT INTO address (id, street_type, street_name, zip_code, number, reference_point, complement, neighborhood, city, state, date_created, date_updated) VALUES
        ('00000000-0000-0000-0001-000000000001', 'Rua', 'XV de Novembro', '80020310', '820', NULL, NULL, 'Centro', 'Curitiba', 'PR', now(), now()),
        ('00000000-0000-0000-0001-000000000003', 'Avenida', 'Ipiranga', '90160093', '1400', NULL, NULL, 'Azenha', 'Porto Alegre', 'RS', now(), now()),
        ('00000000-0000-0000-0001-000000000005', 'Rua', 'Chile', '40020000', '35', NULL, NULL, 'Centro', 'Salvador', 'BA', now(), now()),
        ('00000000-0000-0000-0001-000000000007', 'Avenida', 'Boa Viagem', '51011000', '2500', NULL, NULL, 'Boa Viagem', 'Recife', 'PE', now(), now()),
        ('00000000-0000-0000-0001-000000000009', 'Rua', 'Barão do Rio Branco', '60025060', '910', NULL, NULL, 'Centro', 'Fortaleza', 'CE', now(), now()),
        ('00000000-0000-0000-0001-000000000011', 'Avenida', 'Goiás', '74005010', '222', NULL, NULL, 'Setor Central', 'Goiânia', 'GO', now(), now())
    ON CONFLICT DO NOTHING;

    INSERT INTO natural_person (id, name, email, nickname, cpf, rg, birth_date, gender, phone, cellphone, is_email_verified, date_created, date_updated) VALUES
        ('00000000-0000-0000-0001-000000000013', 'Marcos Oliveira', 'marcos.oliveira@example.com', 'marcos', '40123456720', '216049371', '1984-02-14', 'MALE', '4131234567', '41908641969', TRUE, now(), now()),
        ('00000000-0000-0000-0001-000000000014', 'Luciana Oliveira', 'luciana.oliveira@example.com', 'lu', '40234567805', '230493814', '1986-07-21', 'FEMALE', '4132345678', '41916419746', TRUE, now(), now()),
        ('00000000-0000-0000-0001-000000000015', 'Gabriel Oliveira', 'gabriel.oliveira@example.com', 'gabi', '40345678990', '244938257', '2015-03-11', 'MALE', '4133456789', '41924197523', TRUE, now(), now()),
        ('00000000-0000-0000-0001-000000000016', 'Beatriz Oliveira', 'beatriz.oliveira@example.com', 'bia', '40456789138', '259382583', '2018-05-02', 'FEMALE', '4134567891', '41931975237', TRUE, now(), now()),
        ('00000000-0000-0000-0001-000000000017', 'Roberto Santos', 'roberto.santos@example.com', 'beto', '40567891267', '273825856', '1958-11-30', 'MALE', '5135678912', '51939752384', TRUE, now(), now()),
        ('00000000-0000-0000-0001-000000000018', 'Lucas Santos', 'lucas.santos@example.com', 'luquinhas', '40678912378', '288258599', '2014-01-19', 'MALE', '5136789123', '51947523861', TRUE, now(), now()),
        ('00000000-0000-0000-0001-000000000019', 'Mariana Santos', 'mariana.santos@example.com', 'mari', '40789123460', '302586042', '2010-09-08', 'FEMALE', '5137891234', '51955238638', TRUE, now(), now()),
        ('00000000-0000-0000-0001-000000000020', 'Fernanda Costa', 'fernanda.costa@example.com', 'fe', '40891234535', '315860485', '1991-04-25', 'FEMALE', '7138912345', '71962386415', TRUE, now(), now()),
        ('00000000-0000-0000-0001-000000000021', 'Thiago Costa', 'thiago.costa@example.com', 'thi', '40912345691', '318604928', '2012-06-06', 'MALE', '7139123456', '71963864192', TRUE, now(), now()),
        ('00000000-0000-0000-0001-000000000022', 'Isabela Costa', 'isabela.costa@example.com', 'isa', '41023456710', '333049371', '2014-10-17', 'FEMALE', '7130234567', '71971641969', TRUE, now(), now()),
        ('00000000-0000-0000-0001-000000000023', 'Ricardo Almeida', 'ricardo.almeida@example.com', 'rick', '41134567804', '347493814', '1981-12-03', 'MALE', '8131345678', '81979419746', TRUE, now(), now()),
        ('00000000-0000-0000-0001-000000000024', 'Juliana Almeida', 'juliana.almeida@example.com', 'ju', '41245678981', '361938257', '1983-03-28', 'FEMALE', '8132456789', '81987197523', TRUE, now(), now()),
        ('00000000-0000-0000-0001-000000000025', 'Enzo Almeida', 'enzo.almeida@example.com', 'enzo', '41356789129', '376382583', '2009-02-22', 'MALE', '8133567891', '81994975237', TRUE, now(), now()),
        ('00000000-0000-0000-0001-000000000026', 'Laura Almeida', 'laura.almeida@example.com', 'laurinha', '41467891258', '390825856', '2011-08-14', 'FEMALE', '8134678912', '81902752384', TRUE, now(), now()),
        ('00000000-0000-0000-0001-000000000027', 'Patrícia Lima', 'patricia.lima@example.com', 'paty', '41578912369', '405258599', '1993-09-09', 'FEMALE', '8535789123', '85910523861', TRUE, now(), now()),
        ('00000000-0000-0000-0001-000000000028', 'Davi Lima', 'davi.lima@example.com', 'davi', '41689123451', '419586042', '2018-01-12', 'MALE', '8536891234', '85918238638', TRUE, now(), now()),
        ('00000000-0000-0000-0001-000000000029', 'Alice Lima', 'alice.lima@example.com', 'alice', '41791234526', '432860485', '2019-04-04', 'FEMALE', '8537912345', '85925386415', TRUE, now(), now()),
        ('00000000-0000-0000-0001-000000000030', 'Eduardo Rocha', 'eduardo.rocha@example.com', 'edu', '41812345682', '435604928', '1979-05-16', 'MALE', '6238123456', '62926864192', TRUE, now(), now()),
        ('00000000-0000-0000-0001-000000000031', 'Vitor Rocha', 'vitor.rocha@example.com', 'vitinho', '41923456768', '450049371', '2010-07-27', 'MALE', '6239234567', '62934641969', TRUE, now(), now()),
        ('00000000-0000-0000-0001-000000000032', 'Manuela Rocha', 'manuela.rocha@example.com', 'manu', '42034567897', '464493814', '2013-11-01', 'FEMALE', '6230345678', '62942419746', TRUE, now(), now()),
        ('00000000-0000-0000-0001-000000000033', 'Renata Gomes', 'renata.gomes@example.com', 'renata', '42145678972', '478938257', '1985-01-30', 'FEMALE', '1131456789', '11950197523', TRUE, now(), now()),
        ('00000000-0000-0000-0001-000000000034', 'André Martins', 'andre.martins@example.com', 'andre', '42256789110', '493382583', '1978-10-10', 'MALE', '1132567891', '11957975237', TRUE, now(), now()),
        ('00000000-0000-0000-0001-000000000035', 'Bruna Teixeira', 'bruna.teixeira@example.com', 'bruna', '42367891249', '507825856', '1995-12-12', 'FEMALE', '1133678912', '11965752384', TRUE, now(), now())
    ON CONFLICT DO NOTHING;

    ------------------------------------------------------------------
    -- Pessoas físicas sem vínculo (pré-cadastro)
    -- Ninguém aqui está em grupo familiar, é educando, responsável ou
    -- colaborador: servem para testar busca, autocomplete, vincular a uma
    -- família e a exclusão (permitida, porque não há vínculo).
    -- Documentos gravados sem máscara (RN-GER-01), como chegam pela API:
    --   RG de cada UF: SP (28.451.937-2), SP antigo com dígito X
    --   (37.284.915-X), MG com prefixo da UF (MG-17.293.846), RJ, PR, RS,
    --   BA, CE, PE, SC, DF e GO, com a quantidade de dígitos de cada estado;
    --   Carteira de Identidade Nacional: o número do RG é o próprio CPF
    --   (Kauã e Alex);
    --   criança e bebê só com CPF, que sai na certidão de nascimento;
    --   idosos só com o RG antigo, sem CPF (Benedita e Sebastião).
    -- Mãe e filho (Rosana e Kauã) e pai e filha (Jefferson e Ana Clara)
    -- usam o mesmo e-mail, o que a API permite.
    -- Joana é um cadastro incompleto: sem RG, nascimento, gênero e telefone.
    ------------------------------------------------------------------
    INSERT INTO natural_person (id, name, email, nickname, cpf, rg, birth_date, gender, phone, cellphone, is_email_verified, date_created, date_updated) VALUES
        ('00000000-0000-0000-0002-000000000001', 'Rosana Aparecida Freitas', 'rosana.freitas@example.com', NULL, '50731826426', '284519372', '1982-11-02', 'FEMALE', NULL, '11987452136', TRUE, now(), now()),
        ('00000000-0000-0000-0002-000000000002', 'Kauã Henrique Freitas', 'rosana.freitas@example.com', 'Kauã', '61354072871', '61354072871', '2010-05-14', 'MALE', NULL, NULL, FALSE, now(), now()),
        ('00000000-0000-0000-0002-000000000003', 'Benedita Rosa de Jesus', 'benedita.jesus@example.com', 'Dona Benedita', NULL, 'MG2418567', '1949-03-08', 'FEMALE', '3134251876', NULL, FALSE, now(), now()),
        ('00000000-0000-0000-0002-000000000004', 'Yuki Tanaka Nakamura', 'yuki.nakamura@example.com', 'Yuki', '72965183400', '107654328', '1997-07-19', 'FEMALE', NULL, '41998213457', TRUE, now(), now()),
        ('00000000-0000-0000-0002-000000000005', 'Samir Haddad Neto', 'samir.haddad@example.com', NULL, '83576294074', '214785936', '1975-01-23', 'MALE', '2125487963', '21976431852', TRUE, now(), now()),
        ('00000000-0000-0000-0002-000000000006', 'Jefferson Luiz Ferreira', 'jefferson.ferreira@example.com', 'Jeff', '14827395691', '1342876509', '1990-04-11', 'MALE', NULL, '71991827364', TRUE, now(), now()),
        ('00000000-0000-0000-0002-000000000007', 'Ana Clara dos Santos Ferreira', 'jefferson.ferreira@example.com', 'Aninha', '25438406723', NULL, '2016-09-30', 'FEMALE', NULL, NULL, FALSE, now(), now()),
        ('00000000-0000-0000-0002-000000000008', 'Iracema Potiguara da Silva', 'iracema.silva@example.com', NULL, '36049517800', '2007093651', '1968-12-05', 'FEMALE', '8532561478', '85988214567', TRUE, now(), now()),
        ('00000000-0000-0000-0002-000000000009', 'Maria Eduarda Lopes Cavalcanti', 'duda.cavalcanti@example.com', 'Duda', '47650628990', '7845123', '2005-02-17', 'FEMALE', NULL, '81996327418', TRUE, now(), now()),
        ('00000000-0000-0000-0002-000000000010', 'Gustavo Henrique Bueno', 'gustavo.bueno@example.com', NULL, '58261739040', '6084391275', '1999-08-08', 'MALE', NULL, '51984561237', FALSE, now(), now()),
        ('00000000-0000-0000-0002-000000000011', 'Letícia Vargas Schmitt', 'leticia.schmitt@example.com', 'Lelê', '69872840164', '5612843', '2001-06-21', 'FEMALE', NULL, '48991473682', TRUE, now(), now()),
        ('00000000-0000-0000-0002-000000000012', 'Wellington Souza Brito', 'wellington.brito@example.com', NULL, '70483951250', '3045671', '1986-10-29', 'MALE', '6132458796', '61995218463', TRUE, now(), now()),
        ('00000000-0000-0000-0002-000000000013', 'Raimunda Nonata Araújo', 'raimunda.araujo@example.com', 'Dona Raimunda', '81094062383', '5834127', '1958-05-31', 'FEMALE', '6232147856', NULL, FALSE, now(), now()),
        ('00000000-0000-0000-0002-000000000014', 'Alex Moreira', 'alex.moreira@example.com', 'Alex', '92605173461', '92605173461', '1994-03-14', 'OTHER', NULL, '11974185296', TRUE, now(), now()),
        ('00000000-0000-0000-0002-000000000015', 'Camila Ribeiro Batista', 'camila.batista@example.com', NULL, '13216284557', 'MG17293846', '1998-12-24', 'FEMALE', NULL, '31987412365', TRUE, now(), now()),
        ('00000000-0000-0000-0002-000000000016', 'Lorenzo Batista Ribeiro', 'camila.batista@example.com', NULL, '23827395682', NULL, '2025-07-02', 'MALE', NULL, NULL, FALSE, now(), now()),
        ('00000000-0000-0000-0002-000000000017', 'Sebastião Cardoso', 'sebastiao.cardoso@example.com', 'Seu Tião', NULL, '37284915X', '1947-09-15', 'MALE', '1123456789', NULL, FALSE, now(), now()),
        ('00000000-0000-0000-0002-000000000018', 'Joana Darc Mendes', 'joana.mendes@example.com', NULL, '34438406714', NULL, NULL, NULL, NULL, NULL, FALSE, now(), now())
    ON CONFLICT DO NOTHING;

    INSERT INTO family_group (id, friendly_id, name, household_income, per_capita_income, education_expense, health_expense, housing_expense, number_of_residents, address_id, date_created, date_updated) VALUES
        ('00000000-0000-0000-0001-000000000002', 'FAM-5', 'Família Oliveira', 6200.0, 1550.0, 744.0, 372.0, 1860.0, 4, '00000000-0000-0000-0001-000000000001', now(), now()),
        ('00000000-0000-0000-0001-000000000004', 'FAM-6', 'Família Santos', 2900.0, 966.67, 348.0, 174.0, 870.0, 3, '00000000-0000-0000-0001-000000000003', now(), now()),
        ('00000000-0000-0000-0001-000000000006', 'FAM-7', 'Família Costa', 2100.0, 700.0, 252.0, 126.0, 630.0, 3, '00000000-0000-0000-0001-000000000005', now(), now()),
        ('00000000-0000-0000-0001-000000000008', 'FAM-8', 'Família Almeida', 7400.0, 1850.0, 888.0, 444.0, 2220.0, 4, '00000000-0000-0000-0001-000000000007', now(), now()),
        ('00000000-0000-0000-0001-000000000010', 'FAM-9', 'Família Lima', 1600.0, 533.33, 192.0, 96.0, 480.0, 3, '00000000-0000-0000-0001-000000000009', now(), now()),
        ('00000000-0000-0000-0001-000000000012', 'FAM-10', 'Família Rocha', 3300.0, 1100.0, 396.0, 198.0, 990.0, 3, '00000000-0000-0000-0001-000000000011', now(), now())
    ON CONFLICT DO NOTHING;

    INSERT INTO family_group_natural_person (family_group_id, natural_person_id) VALUES
        ('00000000-0000-0000-0001-000000000002', '00000000-0000-0000-0001-000000000013'),
        ('00000000-0000-0000-0001-000000000002', '00000000-0000-0000-0001-000000000014'),
        ('00000000-0000-0000-0001-000000000002', '00000000-0000-0000-0001-000000000015'),
        ('00000000-0000-0000-0001-000000000002', '00000000-0000-0000-0001-000000000016'),
        ('00000000-0000-0000-0001-000000000004', '00000000-0000-0000-0001-000000000017'),
        ('00000000-0000-0000-0001-000000000004', '00000000-0000-0000-0001-000000000018'),
        ('00000000-0000-0000-0001-000000000004', '00000000-0000-0000-0001-000000000019'),
        ('00000000-0000-0000-0001-000000000006', '00000000-0000-0000-0001-000000000020'),
        ('00000000-0000-0000-0001-000000000006', '00000000-0000-0000-0001-000000000021'),
        ('00000000-0000-0000-0001-000000000006', '00000000-0000-0000-0001-000000000022'),
        ('00000000-0000-0000-0001-000000000008', '00000000-0000-0000-0001-000000000023'),
        ('00000000-0000-0000-0001-000000000008', '00000000-0000-0000-0001-000000000024'),
        ('00000000-0000-0000-0001-000000000008', '00000000-0000-0000-0001-000000000025'),
        ('00000000-0000-0000-0001-000000000008', '00000000-0000-0000-0001-000000000026'),
        ('00000000-0000-0000-0001-000000000010', '00000000-0000-0000-0001-000000000027'),
        ('00000000-0000-0000-0001-000000000010', '00000000-0000-0000-0001-000000000028'),
        ('00000000-0000-0000-0001-000000000010', '00000000-0000-0000-0001-000000000029'),
        ('00000000-0000-0000-0001-000000000012', '00000000-0000-0000-0001-000000000030'),
        ('00000000-0000-0000-0001-000000000012', '00000000-0000-0000-0001-000000000031'),
        ('00000000-0000-0000-0001-000000000012', '00000000-0000-0000-0001-000000000032')
    ON CONFLICT DO NOTHING;

    PERFORM setval('family_group_friendly_id_seq',
        GREATEST(
            (SELECT COALESCE(MAX((substring(friendly_id FROM '^FAM-([0-9]+)$'))::bigint), 0) FROM family_group),
            (SELECT last_value FROM family_group_friendly_id_seq)
        ),
        true
    );

    INSERT INTO learner (id, natural_person_id, institution_id, school_grade_id, interest_arts, scholarship_condition, academic_year, mother_name, school_enrollment, status, date_created, date_updated) VALUES
        ('00000000-0000-0000-0001-000000000036', '00000000-0000-0000-0001-000000000015', '00000000-0000-0000-0000-000000000031', '00000000-0000-0000-0000-000000000076', TRUE, 'PARTIAL', 2026, 'Luciana Oliveira', '2026010', 'ACTIVE', now(), now()),
        ('00000000-0000-0000-0001-000000000037', '00000000-0000-0000-0001-000000000016', '00000000-0000-0000-0000-000000000032', '00000000-0000-0000-0000-000000000073', TRUE, 'NONE', 2026, 'Luciana Oliveira', '2026011', 'ACTIVE', now(), now()),
        ('00000000-0000-0000-0001-000000000038', '00000000-0000-0000-0001-000000000018', '00000000-0000-0000-0000-000000000031', '00000000-0000-0000-0000-000000000077', FALSE, 'FULL', 2026, NULL, '2026012', 'ACTIVE', now(), now()),
        ('00000000-0000-0000-0001-000000000039', '00000000-0000-0000-0001-000000000019', '00000000-0000-0000-0000-000000000031', '00000000-0000-0000-0000-000000000080', TRUE, 'SOCIAL_ASSISTANCE', 2026, NULL, '2026013', 'ACTIVE', now(), now()),
        ('00000000-0000-0000-0001-000000000040', '00000000-0000-0000-0001-000000000021', '00000000-0000-0000-0000-000000000031', '00000000-0000-0000-0000-000000000078', FALSE, 'FULL', 2026, 'Fernanda Costa', '2026014', 'ACTIVE', now(), now()),
        ('00000000-0000-0000-0001-000000000041', '00000000-0000-0000-0001-000000000022', '00000000-0000-0000-0000-000000000031', '00000000-0000-0000-0000-000000000077', TRUE, 'FULL', 2026, 'Fernanda Costa', '2026015', 'ACTIVE', now(), now()),
        ('00000000-0000-0000-0001-000000000042', '00000000-0000-0000-0001-000000000025', '00000000-0000-0000-0000-000000000033', '00000000-0000-0000-0000-000000000082', FALSE, 'NONE', 2026, 'Juliana Almeida', '2026016', 'ACTIVE', now(), now()),
        ('00000000-0000-0000-0001-000000000043', '00000000-0000-0000-0001-000000000026', '00000000-0000-0000-0000-000000000031', '00000000-0000-0000-0000-000000000079', TRUE, 'NONE', 2026, 'Juliana Almeida', '2026017', 'ACTIVE', now(), now()),
        ('00000000-0000-0000-0001-000000000044', '00000000-0000-0000-0001-000000000028', '00000000-0000-0000-0000-000000000032', '00000000-0000-0000-0000-000000000073', FALSE, 'SOCIAL_ASSISTANCE', 2026, 'Patrícia Lima', '2026018', 'ACTIVE', now(), now()),
        ('00000000-0000-0000-0001-000000000045', '00000000-0000-0000-0001-000000000029', '00000000-0000-0000-0000-000000000032', '00000000-0000-0000-0000-000000000072', TRUE, 'SOCIAL_ASSISTANCE', 2026, 'Patrícia Lima', '2026019', 'ACTIVE', now(), now()),
        ('00000000-0000-0000-0001-000000000046', '00000000-0000-0000-0001-000000000031', '00000000-0000-0000-0000-000000000031', '00000000-0000-0000-0000-000000000080', FALSE, 'PARTIAL', 2026, NULL, '2026020', 'DROPPED_OUT', now(), now()),
        ('00000000-0000-0000-0001-000000000047', '00000000-0000-0000-0001-000000000032', '00000000-0000-0000-0000-000000000031', '00000000-0000-0000-0000-000000000078', TRUE, 'PARTIAL', 2026, NULL, '2026021', 'ACTIVE', now(), now())
    ON CONFLICT DO NOTHING;

    INSERT INTO learner_responsible (id, learner_id, natural_person_id, responsible_type, kinship, school_grade_id, date_created, date_updated) VALUES
        ('00000000-0000-0000-0001-000000000048', '00000000-0000-0000-0001-000000000036', '00000000-0000-0000-0001-000000000014', 'COMMUNICATION', 'MOTHER', '00000000-0000-0000-0000-000000000076', now(), now()),
        ('00000000-0000-0000-0001-000000000049', '00000000-0000-0000-0001-000000000036', '00000000-0000-0000-0001-000000000013', 'FINANCIAL', 'FATHER', NULL, now(), now()),
        ('00000000-0000-0000-0001-000000000050', '00000000-0000-0000-0001-000000000037', '00000000-0000-0000-0001-000000000014', 'COMMUNICATION', 'MOTHER', '00000000-0000-0000-0000-000000000073', now(), now()),
        ('00000000-0000-0000-0001-000000000051', '00000000-0000-0000-0001-000000000037', '00000000-0000-0000-0001-000000000013', 'FINANCIAL', 'FATHER', NULL, now(), now()),
        ('00000000-0000-0000-0001-000000000052', '00000000-0000-0000-0001-000000000038', '00000000-0000-0000-0001-000000000017', 'FINANCIAL', 'GRANDFATHER', NULL, now(), now()),
        ('00000000-0000-0000-0001-000000000053', '00000000-0000-0000-0001-000000000038', '00000000-0000-0000-0001-000000000019', 'COMMUNICATION', 'SISTER', NULL, now(), now()),
        ('00000000-0000-0000-0001-000000000054', '00000000-0000-0000-0001-000000000039', '00000000-0000-0000-0001-000000000017', 'FINANCIAL', 'GRANDFATHER', NULL, now(), now()),
        ('00000000-0000-0000-0001-000000000055', '00000000-0000-0000-0001-000000000040', '00000000-0000-0000-0001-000000000020', 'FINANCIAL', 'MOTHER', '00000000-0000-0000-0000-000000000078', now(), now()),
        ('00000000-0000-0000-0001-000000000056', '00000000-0000-0000-0001-000000000041', '00000000-0000-0000-0001-000000000020', 'FINANCIAL', 'MOTHER', '00000000-0000-0000-0000-000000000077', now(), now()),
        ('00000000-0000-0000-0001-000000000057', '00000000-0000-0000-0001-000000000042', '00000000-0000-0000-0001-000000000024', 'COMMUNICATION', 'MOTHER', NULL, now(), now()),
        ('00000000-0000-0000-0001-000000000058', '00000000-0000-0000-0001-000000000042', '00000000-0000-0000-0001-000000000023', 'FINANCIAL', 'FATHER', NULL, now(), now()),
        ('00000000-0000-0000-0001-000000000059', '00000000-0000-0000-0001-000000000043', '00000000-0000-0000-0001-000000000024', 'COMMUNICATION', 'MOTHER', '00000000-0000-0000-0000-000000000079', now(), now()),
        ('00000000-0000-0000-0001-000000000060', '00000000-0000-0000-0001-000000000043', '00000000-0000-0000-0001-000000000023', 'FINANCIAL', 'FATHER', NULL, now(), now()),
        ('00000000-0000-0000-0001-000000000061', '00000000-0000-0000-0001-000000000044', '00000000-0000-0000-0001-000000000027', 'FINANCIAL', 'MOTHER', '00000000-0000-0000-0000-000000000073', now(), now()),
        ('00000000-0000-0000-0001-000000000062', '00000000-0000-0000-0001-000000000045', '00000000-0000-0000-0001-000000000027', 'FINANCIAL', 'MOTHER', '00000000-0000-0000-0000-000000000072', now(), now()),
        ('00000000-0000-0000-0001-000000000063', '00000000-0000-0000-0001-000000000046', '00000000-0000-0000-0001-000000000030', 'FINANCIAL', 'UNCLE', NULL, now(), now()),
        ('00000000-0000-0000-0001-000000000064', '00000000-0000-0000-0001-000000000047', '00000000-0000-0000-0001-000000000030', 'FINANCIAL', 'LEGAL_GUARDIAN', NULL, now(), now())
    ON CONFLICT DO NOTHING;

    INSERT INTO natural_person_role (id, natural_person_id, role, date_created, date_updated) VALUES
        ('00000000-0000-0000-0001-000000000102', '00000000-0000-0000-0001-000000000015', 'LEARNER', now(), now()),
        ('00000000-0000-0000-0001-000000000103', '00000000-0000-0000-0001-000000000016', 'LEARNER', now(), now()),
        ('00000000-0000-0000-0001-000000000104', '00000000-0000-0000-0001-000000000018', 'LEARNER', now(), now()),
        ('00000000-0000-0000-0001-000000000105', '00000000-0000-0000-0001-000000000019', 'LEARNER', now(), now()),
        ('00000000-0000-0000-0001-000000000106', '00000000-0000-0000-0001-000000000021', 'LEARNER', now(), now()),
        ('00000000-0000-0000-0001-000000000107', '00000000-0000-0000-0001-000000000022', 'LEARNER', now(), now()),
        ('00000000-0000-0000-0001-000000000108', '00000000-0000-0000-0001-000000000025', 'LEARNER', now(), now()),
        ('00000000-0000-0000-0001-000000000109', '00000000-0000-0000-0001-000000000026', 'LEARNER', now(), now()),
        ('00000000-0000-0000-0001-000000000110', '00000000-0000-0000-0001-000000000028', 'LEARNER', now(), now()),
        ('00000000-0000-0000-0001-000000000111', '00000000-0000-0000-0001-000000000029', 'LEARNER', now(), now()),
        ('00000000-0000-0000-0001-000000000112', '00000000-0000-0000-0001-000000000031', 'LEARNER', now(), now()),
        ('00000000-0000-0000-0001-000000000113', '00000000-0000-0000-0001-000000000032', 'LEARNER', now(), now()),
        ('00000000-0000-0000-0001-000000000114', '00000000-0000-0000-0001-000000000014', 'RESPONSIBLE', now(), now()),
        ('00000000-0000-0000-0001-000000000115', '00000000-0000-0000-0001-000000000013', 'RESPONSIBLE', now(), now()),
        ('00000000-0000-0000-0001-000000000116', '00000000-0000-0000-0001-000000000017', 'RESPONSIBLE', now(), now()),
        ('00000000-0000-0000-0001-000000000117', '00000000-0000-0000-0001-000000000019', 'RESPONSIBLE', now(), now()),
        ('00000000-0000-0000-0001-000000000118', '00000000-0000-0000-0001-000000000020', 'RESPONSIBLE', now(), now()),
        ('00000000-0000-0000-0001-000000000119', '00000000-0000-0000-0001-000000000024', 'RESPONSIBLE', now(), now()),
        ('00000000-0000-0000-0001-000000000120', '00000000-0000-0000-0001-000000000023', 'RESPONSIBLE', now(), now()),
        ('00000000-0000-0000-0001-000000000121', '00000000-0000-0000-0001-000000000027', 'RESPONSIBLE', now(), now()),
        ('00000000-0000-0000-0001-000000000122', '00000000-0000-0000-0001-000000000030', 'RESPONSIBLE', now(), now())
    ON CONFLICT DO NOTHING;

    INSERT INTO learner_status_history (id, learner_id, type, reason, event_date, date_created, date_updated) VALUES
        ('00000000-0000-0000-0001-000000000099', '00000000-0000-0000-0001-000000000046', 'DROPOUT', 'Começou a trabalhar no período da tarde.', '2026-10-15', now(), now()),
        ('00000000-0000-0000-0001-000000000100', '00000000-0000-0000-0001-000000000047', 'DROPOUT', 'Mudança temporária para outra cidade.', '2026-08-20', now(), now()),
        ('00000000-0000-0000-0001-000000000101', '00000000-0000-0000-0001-000000000047', 'REACTIVATION', 'A família voltou para Goiânia.', '2026-09-10', now(), now())
    ON CONFLICT DO NOTHING;

    INSERT INTO collaborator (id, is_voluntary, natural_person_id, legal_person_id, date_created, date_updated) VALUES
        ('00000000-0000-0000-0001-000000000065', FALSE, '00000000-0000-0000-0001-000000000033', '00000000-0000-0000-0000-000000000032', now(), now()),
        ('00000000-0000-0000-0001-000000000066', FALSE, '00000000-0000-0000-0001-000000000034', '00000000-0000-0000-0000-000000000031', now(), now()),
        ('00000000-0000-0000-0001-000000000067', TRUE, '00000000-0000-0000-0001-000000000035', NULL, now(), now()),
        ('00000000-0000-0000-0001-000000000068', TRUE, '00000000-0000-0000-0001-000000000014', NULL, now(), now())
    ON CONFLICT DO NOTHING;

    INSERT INTO class_group (id, title, student_projections, start_date, end_date, note, program_id, institution_id, active, date_created, date_updated) VALUES
        ('00000000-0000-0000-0000-000000000185', 'Programa Amanhã na Escola - 2º ano, 3º ano - Seg, Qua, Sex', 25, '2026-08-03 13:30:00', '2027-02-03 13:30:00', 'Reforço de alfabetização e acompanhamento de tarefas.', '00000000-0000-0000-0000-000000000044', '00000000-0000-0000-0000-000000000032', TRUE, now(), now()),
        ('00000000-0000-0000-0000-000000000186', 'Programa Resgate Jovem - 8º ano, 9º ano - Ter, Qui', 30, '2027-02-02 08:00:00', '2027-08-02 08:00:00', 'Turma do próximo semestre, matrículas abertas.', '00000000-0000-0000-0000-000000000041', '00000000-0000-0000-0000-000000000031', TRUE, now(), now()),
        ('00000000-0000-0000-0000-000000000187', 'Programa Cidadania Digital - 9º ano, 1º EM - Sáb', 20, '2026-02-07 09:00:00', '2026-06-07 09:00:00', 'Turma do primeiro semestre, já encerrada.', '00000000-0000-0000-0000-000000000042', '00000000-0000-0000-0000-000000000031', TRUE, now(), now())
    ON CONFLICT DO NOTHING;

    INSERT INTO class_school_grade (class_group_id, school_grade_id) VALUES
        ('00000000-0000-0000-0000-000000000185', '00000000-0000-0000-0000-000000000072'),
        ('00000000-0000-0000-0000-000000000185', '00000000-0000-0000-0000-000000000073'),
        ('00000000-0000-0000-0000-000000000186', '00000000-0000-0000-0000-000000000078'),
        ('00000000-0000-0000-0000-000000000186', '00000000-0000-0000-0000-000000000079'),
        ('00000000-0000-0000-0000-000000000187', '00000000-0000-0000-0000-000000000079'),
        ('00000000-0000-0000-0000-000000000187', '00000000-0000-0000-0000-000000000080')
    ON CONFLICT DO NOTHING;

    INSERT INTO class_schedule (id, class_group_id, week_day, start_time, end_time, active, date_created, date_updated) VALUES
        ('00000000-0000-0000-0001-000000000069', '00000000-0000-0000-0000-000000000185', 'MONDAY', '13:30:00', '15:30:00', TRUE, now(), now()),
        ('00000000-0000-0000-0001-000000000070', '00000000-0000-0000-0000-000000000185', 'WEDNESDAY', '13:30:00', '15:30:00', TRUE, now(), now()),
        ('00000000-0000-0000-0001-000000000071', '00000000-0000-0000-0000-000000000185', 'FRIDAY', '13:30:00', '15:30:00', TRUE, now(), now()),
        ('00000000-0000-0000-0001-000000000072', '00000000-0000-0000-0000-000000000186', 'TUESDAY', '08:00:00', '10:00:00', TRUE, now(), now()),
        ('00000000-0000-0000-0001-000000000073', '00000000-0000-0000-0000-000000000186', 'THURSDAY', '08:00:00', '10:00:00', TRUE, now(), now()),
        ('00000000-0000-0000-0001-000000000074', '00000000-0000-0000-0000-000000000187', 'SATURDAY', '09:00:00', '11:00:00', TRUE, now(), now())
    ON CONFLICT DO NOTHING;

    INSERT INTO class_learner (id, class_group_id, learner_id, active, date_created, date_updated) VALUES
        ('00000000-0000-0000-0001-000000000075', '00000000-0000-0000-0000-000000000181', '00000000-0000-0000-0001-000000000036', TRUE, now(), now()),
        ('00000000-0000-0000-0001-000000000076', '00000000-0000-0000-0000-000000000181', '00000000-0000-0000-0001-000000000038', TRUE, now(), now()),
        ('00000000-0000-0000-0001-000000000077', '00000000-0000-0000-0000-000000000181', '00000000-0000-0000-0001-000000000041', TRUE, now(), now()),
        ('00000000-0000-0000-0001-000000000078', '00000000-0000-0000-0000-000000000181', '00000000-0000-0000-0001-000000000047', TRUE, now(), now()),
        ('00000000-0000-0000-0001-000000000079', '00000000-0000-0000-0000-000000000182', '00000000-0000-0000-0001-000000000039', TRUE, now(), now()),
        ('00000000-0000-0000-0001-000000000080', '00000000-0000-0000-0000-000000000182', '00000000-0000-0000-0001-000000000046', FALSE, now(), now()),
        ('00000000-0000-0000-0001-000000000081', '00000000-0000-0000-0000-000000000183', '00000000-0000-0000-0001-000000000042', TRUE, now(), now()),
        ('00000000-0000-0000-0001-000000000082', '00000000-0000-0000-0000-000000000185', '00000000-0000-0000-0001-000000000037', TRUE, now(), now()),
        ('00000000-0000-0000-0001-000000000083', '00000000-0000-0000-0000-000000000185', '00000000-0000-0000-0001-000000000044', TRUE, now(), now()),
        ('00000000-0000-0000-0001-000000000084', '00000000-0000-0000-0000-000000000185', '00000000-0000-0000-0001-000000000045', TRUE, now(), now()),
        ('00000000-0000-0000-0001-000000000085', '00000000-0000-0000-0000-000000000185', '00000000-0000-0000-0000-000000000092', TRUE, now(), now()),
        ('00000000-0000-0000-0001-000000000086', '00000000-0000-0000-0000-000000000186', '00000000-0000-0000-0001-000000000040', TRUE, now(), now()),
        ('00000000-0000-0000-0001-000000000087', '00000000-0000-0000-0000-000000000186', '00000000-0000-0000-0001-000000000043', TRUE, now(), now()),
        ('00000000-0000-0000-0001-000000000088', '00000000-0000-0000-0000-000000000186', '00000000-0000-0000-0001-000000000047', TRUE, now(), now()),
        ('00000000-0000-0000-0001-000000000089', '00000000-0000-0000-0000-000000000187', '00000000-0000-0000-0001-000000000043', TRUE, now(), now()),
        ('00000000-0000-0000-0001-000000000090', '00000000-0000-0000-0000-000000000187', '00000000-0000-0000-0001-000000000039', TRUE, now(), now())
    ON CONFLICT DO NOTHING;

    INSERT INTO class_collaborator (id, class_group_id, collaborator_id, educator_category_item_id, active, date_created, date_updated) VALUES
        ('00000000-0000-0000-0001-000000000091', '00000000-0000-0000-0000-000000000181', '00000000-0000-0000-0001-000000000067', '00000000-0000-0000-0000-000000000053', TRUE, now(), now()),
        ('00000000-0000-0000-0001-000000000092', '00000000-0000-0000-0000-000000000182', '00000000-0000-0000-0001-000000000067', '00000000-0000-0000-0000-000000000055', TRUE, now(), now()),
        ('00000000-0000-0000-0001-000000000093', '00000000-0000-0000-0000-000000000183', '00000000-0000-0000-0001-000000000066', '00000000-0000-0000-0000-000000000061', TRUE, now(), now()),
        ('00000000-0000-0000-0001-000000000094', '00000000-0000-0000-0000-000000000185', '00000000-0000-0000-0001-000000000065', '00000000-0000-0000-0000-000000000058', TRUE, now(), now()),
        ('00000000-0000-0000-0001-000000000095', '00000000-0000-0000-0000-000000000185', '00000000-0000-0000-0001-000000000068', '00000000-0000-0000-0000-000000000059', TRUE, now(), now()),
        ('00000000-0000-0000-0001-000000000096', '00000000-0000-0000-0000-000000000186', '00000000-0000-0000-0001-000000000066', '00000000-0000-0000-0000-000000000052', TRUE, now(), now()),
        ('00000000-0000-0000-0001-000000000097', '00000000-0000-0000-0000-000000000186', '00000000-0000-0000-0000-000000000201', '00000000-0000-0000-0000-000000000051', TRUE, now(), now()),
        ('00000000-0000-0000-0001-000000000098', '00000000-0000-0000-0000-000000000187', '00000000-0000-0000-0001-000000000067', '00000000-0000-0000-0000-000000000054', TRUE, now(), now())
    ON CONFLICT DO NOTHING;

    UPDATE natural_person SET rg = replace(rg, '-', ''), date_updated = now()
    WHERE id::text LIKE '00000000-0000-0000-0000-0000000000%' AND rg ~ '^[0-9]{8}-[0-9]$';

    UPDATE natural_person SET cpf = v.cpf, birth_date = v.birth_date, date_updated = now()
    FROM (VALUES
        ('00000000-0000-0000-0000-000000000014'::uuid, '12345678909', date '2012-06-18', '12345678909', date '2015-06-18'),
        ('00000000-0000-0000-0000-000000000015'::uuid, '98765432100', date '2013-03-09', '98765432100', date '2019-03-09'),
        ('00000000-0000-0000-0000-000000000019'::uuid, '77777777777', date '2007-09-15', '71524386928', date '2009-09-15')
    ) AS v(id, old_cpf, old_birth_date, cpf, birth_date)
    WHERE natural_person.id = v.id AND natural_person.cpf = v.old_cpf AND natural_person.birth_date = v.old_birth_date;

    UPDATE class_group SET start_date = v.start_date, end_date = v.end_date, date_updated = now()
    FROM (VALUES
        ('00000000-0000-0000-0000-000000000181'::uuid, timestamp '2026-02-02 08:00:00', timestamp '2026-08-03 08:00:00', timestamp '2027-02-03 08:00:00'),
        ('00000000-0000-0000-0000-000000000182'::uuid, timestamp '2026-02-02 14:00:00', timestamp '2026-09-01 14:00:00', timestamp '2027-01-01 14:00:00'),
        ('00000000-0000-0000-0000-000000000183'::uuid, timestamp '2026-03-07 09:00:00', timestamp '2026-09-05 09:00:00', timestamp '2027-03-05 09:00:00'),
        ('00000000-0000-0000-0000-000000000184'::uuid, timestamp '2026-02-06 13:00:00', timestamp '2026-08-07 13:00:00', timestamp '2027-01-07 13:00:00')
    ) AS v(id, old_start_date, start_date, end_date)
    WHERE class_group.id = v.id AND class_group.start_date = v.old_start_date;

    ------------------------------------------------------------------
    -- Pessoas que são educando E responsável ao mesmo tempo
    -- Três famílias em que alguém estuda no projeto e também responde por
    -- outro educando da casa (os papéis LEARNER e RESPONSIBLE juntos):
    --   FAM-11 Nascimento (Manaus/AM): Jéssica, 19 anos, cursa o 3º EM e é
    --     a mãe responsável pelo filho Miguel; a avó Sueli é responsável
    --     financeira dos dois.
    --   FAM-12 Andrade (Belém/PA): Larissa, 18 anos, cursa o 3º EM e cuida
    --     da comunicação dos irmãos gêmeos Pedro Henrique e Paulo Henrique;
    --     a tia Cláudia é a responsável financeira dos três.
    --   FAM-13 Moura (Teresina/PI): Tiago, 17 anos, cursa o 2º EM e leva e
    --     busca o sobrinho Heitor; a irmã Kelly é mãe de Heitor e tutora
    --     legal de Tiago.
    -- Ninguém está em turma: os sete aparecem como NO_CLASS.
    ------------------------------------------------------------------
    INSERT INTO address (id, street_type, street_name, zip_code, number, reference_point, complement, neighborhood, city, state, date_created, date_updated) VALUES
        ('00000000-0000-0000-0003-000000000001', 'Rua', 'Lauro Cavalcante', '69020170', '215', 'Perto da feira da Panair', NULL, 'Centro', 'Manaus', 'AM', now(), now()),
        ('00000000-0000-0000-0003-000000000002', 'Travessa', 'Padre Eutíquio', '66023710', '1180', NULL, 'Casa 3', 'Batista Campos', 'Belém', 'PA', now(), now()),
        ('00000000-0000-0000-0003-000000000003', 'Rua', 'Coelho Rodrigues', '64000080', '740', 'Em frente à praça', NULL, 'Centro', 'Teresina', 'PI', now(), now())
    ON CONFLICT DO NOTHING;

    INSERT INTO natural_person (id, name, email, nickname, cpf, rg, birth_date, gender, phone, cellphone, is_email_verified, date_created, date_updated) VALUES
        ('00000000-0000-0000-0003-000000000021', 'Sueli Nascimento', 'sueli.nascimento@example.com', 'Dona Sueli', '61290473803', '1683452', '1972-09-18', 'FEMALE', '9232331478', '92991452367', TRUE, now(), now()),
        ('00000000-0000-0000-0003-000000000022', 'Jéssica Nascimento', 'jessica.nascimento@example.com', 'Jéssica', '71801584966', '2847615', '2007-04-10', 'FEMALE', NULL, '92994518273', TRUE, now(), now()),
        ('00000000-0000-0000-0003-000000000023', 'Miguel Nascimento', 'jessica.nascimento@example.com', NULL, '82412695016', NULL, '2019-08-22', 'MALE', NULL, NULL, FALSE, now(), now()),
        ('00000000-0000-0000-0003-000000000024', 'Cláudia Andrade', 'claudia.andrade@example.com', 'Tia Cláudia', '93023706158', '4517863', '1980-01-27', 'FEMALE', '9132447812', '91988142735', TRUE, now(), now()),
        ('00000000-0000-0000-0003-000000000025', 'Larissa Andrade', 'larissa.andrade@example.com', 'Lari', '14634817217', '7341952', '2008-02-03', 'FEMALE', NULL, '91992613548', TRUE, now(), now()),
        ('00000000-0000-0000-0003-000000000026', 'Pedro Henrique Andrade', 'claudia.andrade@example.com', NULL, '25245928396', '8812734', '2015-06-12', 'MALE', NULL, NULL, FALSE, now(), now()),
        ('00000000-0000-0000-0003-000000000027', 'Paulo Henrique Andrade', 'claudia.andrade@example.com', NULL, '36856039492', NULL, '2015-06-12', 'MALE', NULL, NULL, FALSE, now(), now()),
        ('00000000-0000-0000-0003-000000000028', 'Kelly Moura', 'kelly.moura@example.com', NULL, '47467140533', '3172846', '1995-11-30', 'FEMALE', NULL, '86994127356', TRUE, now(), now()),
        ('00000000-0000-0000-0003-000000000029', 'Tiago Moura', 'tiago.moura@example.com', 'Tiaguinho', '58078251602', '3561987', '2009-03-17', 'MALE', NULL, '86981534726', FALSE, now(), now()),
        ('00000000-0000-0000-0003-000000000030', 'Heitor Moura', 'kelly.moura@example.com', NULL, '69689362755', NULL, '2016-10-05', 'MALE', NULL, NULL, FALSE, now(), now())
    ON CONFLICT DO NOTHING;

    INSERT INTO family_group (id, friendly_id, name, household_income, per_capita_income, education_expense, health_expense, housing_expense, number_of_residents, address_id, date_created, date_updated) VALUES
        ('00000000-0000-0000-0003-000000000011', 'FAM-11', 'Família Nascimento', '2400.0', '800.0', '180.0', '220.0', '650.0', 3, '00000000-0000-0000-0003-000000000001', now(), now()),
        ('00000000-0000-0000-0003-000000000012', 'FAM-12', 'Família Andrade', '3100.0', '775.0', '320.0', '150.0', '900.0', 4, '00000000-0000-0000-0003-000000000002', now(), now()),
        ('00000000-0000-0000-0003-000000000013', 'FAM-13', 'Família Moura', '1900.0', '633.33', '140.0', '90.0', '500.0', 3, '00000000-0000-0000-0003-000000000003', now(), now())
    ON CONFLICT DO NOTHING;

    INSERT INTO family_group_natural_person (family_group_id, natural_person_id) VALUES
        ('00000000-0000-0000-0003-000000000011', '00000000-0000-0000-0003-000000000021'),
        ('00000000-0000-0000-0003-000000000011', '00000000-0000-0000-0003-000000000022'),
        ('00000000-0000-0000-0003-000000000011', '00000000-0000-0000-0003-000000000023'),
        ('00000000-0000-0000-0003-000000000012', '00000000-0000-0000-0003-000000000024'),
        ('00000000-0000-0000-0003-000000000012', '00000000-0000-0000-0003-000000000025'),
        ('00000000-0000-0000-0003-000000000012', '00000000-0000-0000-0003-000000000026'),
        ('00000000-0000-0000-0003-000000000012', '00000000-0000-0000-0003-000000000027'),
        ('00000000-0000-0000-0003-000000000013', '00000000-0000-0000-0003-000000000028'),
        ('00000000-0000-0000-0003-000000000013', '00000000-0000-0000-0003-000000000029'),
        ('00000000-0000-0000-0003-000000000013', '00000000-0000-0000-0003-000000000030')
    ON CONFLICT DO NOTHING;

    PERFORM setval('family_group_friendly_id_seq',
        GREATEST(
            (SELECT COALESCE(MAX((substring(friendly_id FROM '^FAM-([0-9]+)$'))::bigint), 0) FROM family_group),
            (SELECT last_value FROM family_group_friendly_id_seq)
        ),
        true
    );

    INSERT INTO learner (id, natural_person_id, institution_id, school_grade_id, interest_arts, scholarship_condition, academic_year, mother_name, school_enrollment, status, date_created, date_updated) VALUES
        ('00000000-0000-0000-0003-000000000041', '00000000-0000-0000-0003-000000000022', '00000000-0000-0000-0000-000000000031', '00000000-0000-0000-0000-000000000082', TRUE, 'PARTIAL', 2026, 'Sueli Nascimento', '2026022', 'ACTIVE', now(), now()),
        ('00000000-0000-0000-0003-000000000042', '00000000-0000-0000-0003-000000000023', '00000000-0000-0000-0000-000000000031', '00000000-0000-0000-0000-000000000072', TRUE, 'FULL', 2026, 'Jéssica Nascimento', '2026023', 'ACTIVE', now(), now()),
        ('00000000-0000-0000-0003-000000000043', '00000000-0000-0000-0003-000000000025', '00000000-0000-0000-0000-000000000033', '00000000-0000-0000-0000-000000000082', FALSE, 'SOCIAL_ASSISTANCE', 2026, 'Rosângela Andrade', '2026024', 'ACTIVE', now(), now()),
        ('00000000-0000-0000-0003-000000000044', '00000000-0000-0000-0003-000000000026', '00000000-0000-0000-0000-000000000032', '00000000-0000-0000-0000-000000000075', TRUE, 'SOCIAL_ASSISTANCE', 2026, 'Rosângela Andrade', '2026025', 'ACTIVE', now(), now()),
        ('00000000-0000-0000-0003-000000000045', '00000000-0000-0000-0003-000000000027', '00000000-0000-0000-0000-000000000032', '00000000-0000-0000-0000-000000000075', FALSE, 'SOCIAL_ASSISTANCE', 2026, 'Rosângela Andrade', '2026026', 'ACTIVE', now(), now()),
        ('00000000-0000-0000-0003-000000000046', '00000000-0000-0000-0003-000000000029', '00000000-0000-0000-0000-000000000034', '00000000-0000-0000-0000-000000000081', FALSE, 'PARTIAL', 2026, 'Socorro Moura', '2026027', 'ACTIVE', now(), now()),
        ('00000000-0000-0000-0003-000000000047', '00000000-0000-0000-0003-000000000030', '00000000-0000-0000-0000-000000000034', '00000000-0000-0000-0000-000000000074', TRUE, 'FULL', 2026, 'Kelly Moura', '2026028', 'ACTIVE', now(), now())
    ON CONFLICT DO NOTHING;

    INSERT INTO learner_responsible (id, learner_id, natural_person_id, responsible_type, kinship, school_grade_id, date_created, date_updated) VALUES
        ('00000000-0000-0000-0003-000000000051', '00000000-0000-0000-0003-000000000041', '00000000-0000-0000-0003-000000000021', 'FINANCIAL', 'MOTHER', NULL, now(), now()),
        ('00000000-0000-0000-0003-000000000052', '00000000-0000-0000-0003-000000000042', '00000000-0000-0000-0003-000000000022', 'COMMUNICATION', 'MOTHER', '00000000-0000-0000-0000-000000000082', now(), now()),
        ('00000000-0000-0000-0003-000000000053', '00000000-0000-0000-0003-000000000042', '00000000-0000-0000-0003-000000000021', 'FINANCIAL', 'GRANDMOTHER', NULL, now(), now()),
        ('00000000-0000-0000-0003-000000000054', '00000000-0000-0000-0003-000000000043', '00000000-0000-0000-0003-000000000024', 'FINANCIAL', 'AUNT', '00000000-0000-0000-0000-000000000079', now(), now()),
        ('00000000-0000-0000-0003-000000000055', '00000000-0000-0000-0003-000000000044', '00000000-0000-0000-0003-000000000025', 'COMMUNICATION', 'SISTER', '00000000-0000-0000-0000-000000000082', now(), now()),
        ('00000000-0000-0000-0003-000000000056', '00000000-0000-0000-0003-000000000044', '00000000-0000-0000-0003-000000000024', 'FINANCIAL', 'AUNT', '00000000-0000-0000-0000-000000000079', now(), now()),
        ('00000000-0000-0000-0003-000000000057', '00000000-0000-0000-0003-000000000045', '00000000-0000-0000-0003-000000000025', 'COMMUNICATION', 'SISTER', '00000000-0000-0000-0000-000000000082', now(), now()),
        ('00000000-0000-0000-0003-000000000058', '00000000-0000-0000-0003-000000000045', '00000000-0000-0000-0003-000000000024', 'FINANCIAL', 'AUNT', '00000000-0000-0000-0000-000000000079', now(), now()),
        ('00000000-0000-0000-0003-000000000059', '00000000-0000-0000-0003-000000000046', '00000000-0000-0000-0003-000000000028', 'FINANCIAL', 'LEGAL_GUARDIAN', '00000000-0000-0000-0000-000000000082', now(), now()),
        ('00000000-0000-0000-0003-000000000060', '00000000-0000-0000-0003-000000000047', '00000000-0000-0000-0003-000000000028', 'FINANCIAL', 'MOTHER', '00000000-0000-0000-0000-000000000082', now(), now()),
        ('00000000-0000-0000-0003-000000000061', '00000000-0000-0000-0003-000000000047', '00000000-0000-0000-0003-000000000029', 'COMMUNICATION', 'UNCLE', '00000000-0000-0000-0000-000000000081', now(), now())
    ON CONFLICT DO NOTHING;

    INSERT INTO natural_person_role (id, natural_person_id, role, date_created, date_updated) VALUES
        ('00000000-0000-0000-0003-000000000071', '00000000-0000-0000-0003-000000000022', 'LEARNER', now(), now()),
        ('00000000-0000-0000-0003-000000000072', '00000000-0000-0000-0003-000000000023', 'LEARNER', now(), now()),
        ('00000000-0000-0000-0003-000000000073', '00000000-0000-0000-0003-000000000025', 'LEARNER', now(), now()),
        ('00000000-0000-0000-0003-000000000074', '00000000-0000-0000-0003-000000000026', 'LEARNER', now(), now()),
        ('00000000-0000-0000-0003-000000000075', '00000000-0000-0000-0003-000000000027', 'LEARNER', now(), now()),
        ('00000000-0000-0000-0003-000000000076', '00000000-0000-0000-0003-000000000029', 'LEARNER', now(), now()),
        ('00000000-0000-0000-0003-000000000077', '00000000-0000-0000-0003-000000000030', 'LEARNER', now(), now()),
        ('00000000-0000-0000-0003-000000000078', '00000000-0000-0000-0003-000000000021', 'RESPONSIBLE', now(), now()),
        ('00000000-0000-0000-0003-000000000079', '00000000-0000-0000-0003-000000000022', 'RESPONSIBLE', now(), now()),
        ('00000000-0000-0000-0003-000000000080', '00000000-0000-0000-0003-000000000024', 'RESPONSIBLE', now(), now()),
        ('00000000-0000-0000-0003-000000000081', '00000000-0000-0000-0003-000000000025', 'RESPONSIBLE', now(), now()),
        ('00000000-0000-0000-0003-000000000082', '00000000-0000-0000-0003-000000000028', 'RESPONSIBLE', now(), now()),
        ('00000000-0000-0000-0003-000000000083', '00000000-0000-0000-0003-000000000029', 'RESPONSIBLE', now(), now())
    ON CONFLICT DO NOTHING;

    RAISE NOTICE 'Seeder aplicado com sucesso.';
END $$;
