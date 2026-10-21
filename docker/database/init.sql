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
        ('00000000-0000-0000-0000-000000000011', 'Maria Silva', 'maria.silva@example.com', 'maria', '52998224725', '12345678-9', '1995-04-12', 'FEMALE', '1130001111', '11911112222', TRUE, now(), now()),
        ('00000000-0000-0000-0000-000000000012', 'João Pereira', 'joao.pereira@example.com', 'joaop', '11144477735', '23456789-0', '1990-08-03', 'MALE', '1130002222', '11933334444', TRUE, now(), now()),
        ('00000000-0000-0000-0000-000000000013', 'Ana Souza', 'ana.souza@example.com', 'ana', '39053344705', '34567890-1', '2000-01-25', 'FEMALE', '3130003333', '31955556666', TRUE, now(), now()),
        ('00000000-0000-0000-0000-000000000014', 'Pedro Silva', 'pedro.silva@example.com', 'pedros', '12345678909', '44556677-8', '2012-06-18', 'MALE', '1130004444', '11944445555', TRUE, now(), now()),
        ('00000000-0000-0000-0000-000000000015', 'Júlia Pereira', 'julia.pereira@example.com', 'juliap', '98765432100', '55667788-9', '2013-03-09', 'FEMALE', '1130005555', '11955556666', TRUE, now(), now()),
        ('00000000-0000-0000-0000-000000000016', 'Carlos Souza', 'carlos.souza@example.com', 'carloss', '24681357928', '66778899-0', '2011-11-27', 'MALE', '3130006666', '31966667777', TRUE, now(), now()),
        ('00000000-0000-0000-0000-000000000017', 'Rafael Silva', 'rafael.silva@example.com', 'rafaels', '10293847541', '77889900-1', '1988-07-05', 'MALE', '1130007777', '11977778888', TRUE, now(), now()),
        ('00000000-0000-0000-0000-000000000018', 'Paula Pereira', 'paula.pereira@example.com', 'paulap', '56789012303', '88990011-2', '1989-12-02', 'FEMALE', '1130008888', '11988889999', TRUE, now(), now()),
        ('00000000-0000-0000-0000-000000000019', 'Camila Souza', 'camila.souza@example.com', 'camilas', '77777777777', '99001122-3', '2007-09-15', 'FEMALE', '3130009999', '31999990000', TRUE, now(), now()),
        ('00000000-0000-0000-0000-000000000020', 'Diego Ramos', 'diego.ramos@example.com', 'diegor', '31415926590', '10111213-4', '1987-05-20', 'MALE', '1930001111', '19911112222', TRUE, now(), now()),
        ('00000000-0000-0000-0000-000000000021', 'Sofia Ramos', 'sofia.ramos@example.com', 'sofias', '20246813520', '11121314-5', '2014-08-30', 'FEMALE', '1930002222', '19922223333', TRUE, now(), now()),
        ('00000000-0000-0000-0000-000000000022', 'Helena Ramos', 'helena.ramos@example.com', 'helenar', '65432109824', '12131415-6', '1986-10-11', 'FEMALE', '1930003333', '19933334444', TRUE, now(), now())
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
        ('00000000-0000-0000-0000-000000000181', 'Programa Resgate Jovem - 6º ano, 7º ano - Seg, Qua', 30, '2026-02-02 08:00:00', '2026-08-02 08:00:00', 'Turma de reforço escolar no contraturno.', '00000000-0000-0000-0000-000000000041', '00000000-0000-0000-0000-000000000031', TRUE, now(), now()),
        ('00000000-0000-0000-0000-000000000182', 'Programa Cidadania Digital - 1º EM - Ter, Qui', 25, '2026-02-02 14:00:00', '2026-06-02 14:00:00', 'Laboratório de informática, duas turmas por semana.', '00000000-0000-0000-0000-000000000042', '00000000-0000-0000-0000-000000000031', TRUE, now(), now()),
        ('00000000-0000-0000-0000-000000000183', 'Programa Educar para Vencer - 3º EM - Sáb', 20, '2026-03-07 09:00:00', '2026-09-07 09:00:00', 'Encontro de sábado para conclusão do ensino médio.', '00000000-0000-0000-0000-000000000045', '00000000-0000-0000-0000-000000000033', TRUE, now(), now()),
        ('00000000-0000-0000-0000-000000000184', 'Programa Amanhã na Escola - 2º ano - Sex', 18, '2026-02-06 13:00:00', '2026-07-06 13:00:00', NULL, '00000000-0000-0000-0000-000000000044', '00000000-0000-0000-0000-000000000032', FALSE, now(), now())
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

    RAISE NOTICE 'Seeder aplicado com sucesso.';
END $$;
