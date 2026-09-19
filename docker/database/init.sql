DO $$
BEGIN
    IF EXISTS (SELECT 1 FROM users WHERE email = 'admin@projetoresgate.com') THEN
        RAISE NOTICE 'Seeder ja executado. Nenhuma acao necessaria.';
        RETURN;
    END IF;

    INSERT INTO users (id, email, password, name, nickname, token_version, date_created, date_updated) VALUES
        ('00000000-0000-0000-0000-000000000001', 'admin@projetoresgate.com', '$2b$10$hCY2pM6q1FgBXil.4SGUOOQC1sdRqBsW5qSEtdXdLh0VqWN9DzNiu', 'Administrador', 'admin', 0, now(), now()),
        ('00000000-0000-0000-0000-000000000002', 'maria.silva@example.com', '$2b$10$kF0WoXQ.fZbm.LXkXTMVt.H7.cuNDVOKziUfJmqqHAK8DtafYXrcG', 'Maria Silva', 'maria', 0, now(), now()),
        ('00000000-0000-0000-0000-000000000003', 'contato@projetoresgate.com.br', '$2b$10$kF0WoXQ.fZbm.LXkXTMVt.H7.cuNDVOKziUfJmqqHAK8DtafYXrcG', 'Contato Projeto Resgate', 'projeto', 0, now(), now());

    INSERT INTO user_roles (user_id, role) VALUES
        ('00000000-0000-0000-0000-000000000001', 'ADMIN'),
        ('00000000-0000-0000-0000-000000000001', 'ADMIN'),
        ('00000000-0000-0000-0000-000000000002', 'ADMIN'),
        ('00000000-0000-0000-0000-000000000002', 'ADMIN'),
        ('00000000-0000-0000-0000-000000000003', 'ADMIN'),
        ('00000000-0000-0000-0000-000000000003', 'ADMIN');

    INSERT INTO natural_person (id, name, email, nickname, cpf, rg, birth_date, gender, phone, cellphone, is_email_verified, date_created, date_updated) VALUES
        ('00000000-0000-0000-0000-000000000011', 'Maria Silva', 'maria.silva@example.com', 'maria', '52998224725', '12345678-9', '1995-04-12', 'FEMALE', '1130001111', '11911112222', TRUE, now(), now()),
        ('00000000-0000-0000-0000-000000000012', 'João Pereira', 'joao.pereira@example.com', 'joaop', '11144477735', '23456789-0', '1990-08-03', 'MALE', '1130002222', '11933334444', TRUE, now(), now()),
        ('00000000-0000-0000-0000-000000000013', 'Ana Souza', 'ana.souza@example.com', 'ana', '39053344705', '34567890-1', '2000-01-25', 'FEMALE', '3130003333', '31955556666', TRUE, now(), now());

    INSERT INTO address (id, zip_code, number, complement, neighborhood, city, state, date_created, date_updated) VALUES
        ('00000000-0000-0000-0000-000000000021', '01001000', '100', 'Sala 1', 'Centro', 'São Paulo', 'SP', now(), now()),
        ('00000000-0000-0000-0000-000000000022', '01310100', '205', 'Andar 3', 'Bela Vista', 'São Paulo', 'SP', now(), now()),
        ('00000000-0000-0000-0000-000000000023', '30130010', '400', NULL, 'Centro', 'Belo Horizonte', 'MG', now(), now());

    INSERT INTO legal_person (id, cnpj, corporate_name, trade_name, display_name, main_cnae_code, registration_status, company_status, address_id, representative_name, representative_cellphone, representative_phone, representative_email, date_created, date_updated) VALUES
        ('00000000-0000-0000-0000-000000000031', '11222333000181', 'Instituto Projeto Resgate', 'Projeto Resgate', 'Projeto Resgate', '8550301', 'ACTIVE', 'ACTIVE', '00000000-0000-0000-0000-000000000021', 'Carlos Andrade', '11977778888', '1133334444', 'carlos@projetoresgate.com.br', now(), now()),
        ('00000000-0000-0000-0000-000000000032', '11444777000161', 'Fundação Amanhã', 'Fundação Amanhã', 'Fundação Amanhã', '8550301', 'ACTIVE', 'ACTIVE', '00000000-0000-0000-0000-000000000022', 'Fernanda Lima', '11966665555', '1134445555', 'fernanda@fundacaoamanha.org', now(), now()),
        ('00000000-0000-0000-0000-000000000033', '12345678000190', 'Associação Educar Mais', 'Educar Mais', 'Educar Mais', '8550301', 'ACTIVE', 'ACTIVE', '00000000-0000-0000-0000-000000000023', 'Paulo Ramos', '31955554444', '3132223333', 'paulo@educarmais.org', now(), now());

    INSERT INTO program (id, name, web_site_url, status, institution_id, date_created, date_updated) VALUES
        ('00000000-0000-0000-0000-000000000041', 'Programa Resgate Jovem', 'https://resgatejovem.org', 'ACTIVE', '00000000-0000-0000-0000-000000000031', now(), now()),
        ('00000000-0000-0000-0000-000000000042', 'Programa Cidadania Digital', 'https://cidadaniadigital.org', 'ACTIVE', '00000000-0000-0000-0000-000000000031', now(), now()),
        ('00000000-0000-0000-0000-000000000043', 'Programa Primeiro Emprego', 'https://primeiroemprego.org', 'INACTIVE', '00000000-0000-0000-0000-000000000031', now(), now());

    INSERT INTO educator_category_item (id, name, program_id, date_created, date_updated) VALUES
        ('00000000-0000-0000-0000-000000000051', 'Orientação Profissional', '00000000-0000-0000-0000-000000000041', now(), now()),
        ('00000000-0000-0000-0000-000000000052', 'Reforço Escolar', '00000000-0000-0000-0000-000000000041', now(), now()),
        ('00000000-0000-0000-0000-000000000053', 'Esporte e Cultura', '00000000-0000-0000-0000-000000000041', now(), now()),
        ('00000000-0000-0000-0000-000000000054', 'Inclusão Digital', '00000000-0000-0000-0000-000000000042', now(), now()),
        ('00000000-0000-0000-0000-000000000055', 'Informática Básica', '00000000-0000-0000-0000-000000000042', now(), now()),
        ('00000000-0000-0000-0000-000000000056', 'Currículo e Entrevista', '00000000-0000-0000-0000-000000000043', now(), now()),
        ('00000000-0000-0000-0000-000000000057', 'Capacitação Profissional', '00000000-0000-0000-0000-000000000043', now(), now());

    RAISE NOTICE 'Seeder aplicado com sucesso.';
END $$;