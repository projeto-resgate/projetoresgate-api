package com.projetoresgate.projetoresgate_api.core.academic.collaborator.repository;

import com.projetoresgate.projetoresgate_api.core.academic.collaborator.domain.Collaborator;
import com.projetoresgate.projetoresgate_api.core.identity.address.domain.Address;
import com.projetoresgate.projetoresgate_api.core.identity.legalperson.domain.LegalPerson;
import com.projetoresgate.projetoresgate_api.core.identity.legalperson.domain.enums.CompanyStatus;
import com.projetoresgate.projetoresgate_api.core.identity.legalperson.domain.enums.RegistrationStatus;
import com.projetoresgate.projetoresgate_api.core.identity.legalperson.repository.LegalPersonRepository;
import com.projetoresgate.projetoresgate_api.core.identity.naturalperson.domain.NaturalPerson;
import com.projetoresgate.projetoresgate_api.core.identity.naturalperson.repository.NaturalPersonRepository;
import com.projetoresgate.projetoresgate_api.shared.testcontainers.PostgresIntegrationTest;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertEquals;

@DataJpaTest
@DisplayName("CollaboratorRepository - Integração")
class CollaboratorRepositoryIntegrationTest extends PostgresIntegrationTest {

    private static final AtomicInteger SEQUENCE = new AtomicInteger(0);

    @Autowired
    private CollaboratorRepository repository;

    @Autowired
    private NaturalPersonRepository naturalPersonRepository;

    @Autowired
    private LegalPersonRepository legalPersonRepository;

    @Autowired
    private EntityManager entityManager;

    private NaturalPerson givenNaturalPerson(String name, String nickname, String cpf) {
        int sequence = SEQUENCE.incrementAndGet();
        return naturalPersonRepository.save(NaturalPerson.create(
                name, "colaborador" + sequence + "@example.com", nickname, cpf, null, null, null, null, null));
    }

    private LegalPerson givenLegalPerson(String corporateName, String tradeName) {
        int sequence = SEQUENCE.incrementAndGet();
        Address address = Address.create("Rua", "Das Flores", "60000-000", "100", null, null,
                "Centro", "Fortaleza", "CE");
        return legalPersonRepository.save(LegalPerson.create(
                String.format("%014d", 30000000000000L + sequence), corporateName, tradeName, corporateName,
                null, RegistrationStatus.ACTIVE, CompanyStatus.ACTIVE, address, null));
    }

    private Collaborator givenCollaborator(String name, String nickname, String cpf, LegalPerson legalPerson) {
        NaturalPerson naturalPerson = givenNaturalPerson(name, nickname, cpf);
        return repository.save(Collaborator.create(
                false, naturalPerson.getId(), legalPerson == null ? null : legalPerson.getId()));
    }

    private Page<Collaborator> search(String term) {
        entityManager.flush();
        entityManager.clear();
        return repository.search(term, PageRequest.of(0, 10));
    }

    private List<UUID> naturalPersonIds(Page<Collaborator> page) {
        return page.getContent().stream().map(Collaborator::getNaturalPersonId).toList();
    }

    @Test
    @DisplayName("Deve listar todos ordenados pelo nome da pessoa física quando o termo é nulo")
    void search_ShouldListAllOrderedByName() {
        Collaborator carla = givenCollaborator("Carla Dias", null, null, null);
        Collaborator ana = givenCollaborator("Ana Souza", null, null, null);
        Collaborator bruno = givenCollaborator("Bruno Lima", null, null, null);

        Page<Collaborator> result = search(null);

        assertEquals(3, result.getTotalElements());
        assertEquals(List.of(ana.getNaturalPersonId(), bruno.getNaturalPersonId(), carla.getNaturalPersonId()),
                naturalPersonIds(result));
    }

    @Test
    @DisplayName("Deve filtrar pelo nome da pessoa física ignorando maiúsculas")
    void search_ShouldFilterByNaturalPersonName() {
        givenCollaborator("Ana Souza", null, null, null);
        Collaborator bruno = givenCollaborator("Bruno Lima", null, null, null);

        Page<Collaborator> result = search("BRU");

        assertEquals(1, result.getTotalElements());
        assertEquals(bruno.getNaturalPersonId(), naturalPersonIds(result).getFirst());
    }

    @Test
    @DisplayName("Deve filtrar pelo apelido da pessoa física")
    void search_ShouldFilterByNaturalPersonNickname() {
        givenCollaborator("Ana Souza", "Aninha", null, null);
        Collaborator bruno = givenCollaborator("Bruno Lima", "Brunão", null, null);

        Page<Collaborator> result = search("brunão");

        assertEquals(1, result.getTotalElements());
        assertEquals(bruno.getNaturalPersonId(), naturalPersonIds(result).getFirst());
    }

    @Test
    @DisplayName("Deve filtrar pelo CPF da pessoa física")
    void search_ShouldFilterByNaturalPersonCpf() {
        givenCollaborator("Ana Souza", null, "11122233344", null);
        Collaborator bruno = givenCollaborator("Bruno Lima", null, "55566677788", null);

        Page<Collaborator> result = search("666777");

        assertEquals(1, result.getTotalElements());
        assertEquals(bruno.getNaturalPersonId(), naturalPersonIds(result).getFirst());
    }

    @Test
    @DisplayName("Deve filtrar pela razão social da pessoa jurídica")
    void search_ShouldFilterByLegalPersonCorporateName() {
        givenCollaborator("Ana Souza", null, null, null);
        LegalPerson organization = givenLegalPerson("Instituto Semear", null);
        Collaborator bruno = givenCollaborator("Bruno Lima", null, null, organization);

        Page<Collaborator> result = search("semear");

        assertEquals(1, result.getTotalElements());
        assertEquals(bruno.getNaturalPersonId(), naturalPersonIds(result).getFirst());
    }

    @Test
    @DisplayName("Deve filtrar pelo nome fantasia da pessoa jurídica")
    void search_ShouldFilterByLegalPersonTradeName() {
        LegalPerson organization = givenLegalPerson("Instituto Semear", "Semear Cursos");
        Collaborator ana = givenCollaborator("Ana Souza", null, null, organization);
        givenCollaborator("Bruno Lima", null, null, null);

        Page<Collaborator> result = search("cursos");

        assertEquals(1, result.getTotalElements());
        assertEquals(ana.getNaturalPersonId(), naturalPersonIds(result).getFirst());
    }
}
