package com.projetoresgate.projetoresgate_api.core.academic.learner.service;

import com.projetoresgate.projetoresgate_api.core.academic.learner.api.dto.LearnerResponse;
import com.projetoresgate.projetoresgate_api.core.academic.learner.domain.Learner;
import com.projetoresgate.projetoresgate_api.core.academic.learner.domain.Responsible;
import com.projetoresgate.projetoresgate_api.core.academic.learner.domain.enums.Kinship;
import com.projetoresgate.projetoresgate_api.core.academic.learner.domain.enums.LearnerStatus;
import com.projetoresgate.projetoresgate_api.core.academic.learner.domain.enums.ResponsibleType;
import com.projetoresgate.projetoresgate_api.core.academic.learner.domain.enums.ScholarshipCondition;
import com.projetoresgate.projetoresgate_api.core.academic.learner.repository.LearnerRepository;
import com.projetoresgate.projetoresgate_api.core.academic.learner.repository.ResponsibleRepository;
import com.projetoresgate.projetoresgate_api.core.academic.learner.usecase.FindLearnerByIdUseCase;
import com.projetoresgate.projetoresgate_api.core.academic.learner.usecase.command.ResponsibleCommand;
import com.projetoresgate.projetoresgate_api.core.academic.learner.usecase.command.UpdateLearnerCommand;
import com.projetoresgate.projetoresgate_api.core.academic.learner.usecase.query.FindLearnerByIdQuery;
import com.projetoresgate.projetoresgate_api.core.academic.schoolgrade.repository.SchoolGradeRepository;
import com.projetoresgate.projetoresgate_api.core.identity.familygroup.repository.FamilyGroupRepository;
import com.projetoresgate.projetoresgate_api.core.identity.legalperson.repository.LegalPersonRepository;
import com.projetoresgate.projetoresgate_api.core.identity.naturalperson.repository.NaturalPersonRepository;
import com.projetoresgate.projetoresgate_api.infrastructure.exception.InternalException;
import com.projetoresgate.projetoresgate_api.infrastructure.exception.ResourceNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InOrder;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("UpdateLearnerService - Test")
class UpdateLearnerServiceTest {

    private static final UUID FAMILY_GROUP_ID = UUID.randomUUID();
    private static final UUID NATURAL_PERSON_ID = UUID.randomUUID();
    private static final UUID RESPONSIBLE_PERSON_ID = UUID.randomUUID();

    @Mock
    private LearnerRepository repository;

    @Mock
    private FamilyGroupRepository familyGroupRepository;

    @Mock
    private NaturalPersonRepository naturalPersonRepository;

    @Mock
    private LegalPersonRepository legalPersonRepository;

    @Mock
    private SchoolGradeRepository schoolGradeRepository;

    @Mock
    private FindLearnerByIdUseCase findLearnerByIdUseCase;

    @Mock
    private ResponsibleRepository responsibleRepository;

    @Mock
    private LearnerRoleSynchronizer roleSynchronizer;

    @BeforeEach
    void givenPersonBelongsToFamilyGroup() {
        lenient().when(familyGroupRepository.existsNaturalPersonMember(any(UUID.class), any(UUID.class)))
                .thenReturn(true);
    }

    private void givenNotMemberOfFamilyGroup(UUID naturalPersonId) {
        when(familyGroupRepository.existsNaturalPersonMember(FAMILY_GROUP_ID, naturalPersonId)).thenReturn(false);
    }

    @InjectMocks
    private UpdateLearnerService service;

    private static Learner givenLearner() {
        return Learner.create(NATURAL_PERSON_ID, null, null, false, null, 2026, null, null);
    }

    private static UpdateLearnerCommand givenCommand(List<ResponsibleCommand> responsibleList) {
        return new UpdateLearnerCommand(null, NATURAL_PERSON_ID, FAMILY_GROUP_ID, null, null,
                false, null, 2026, null, null, responsibleList);
    }

    private void givenFindByIdResponds() {
        when(findLearnerByIdUseCase.handle(any(FindLearnerByIdQuery.class)))
                .thenReturn(new LearnerResponse(UUID.randomUUID(), NATURAL_PERSON_ID, null, null,
                        false, null, 2026, null, null, LearnerStatus.ACTIVE, List.of(), null));
    }

    @Test
    @DisplayName("Deve atualizar os dados escolares do educando existente")
    void handle_ShouldUpdateSchoolData() {
        Learner existing = givenLearner();
        when(repository.findByIdOrThrow(existing.getId())).thenReturn(existing);
        when(repository.save(any(Learner.class))).thenAnswer(i -> i.getArgument(0));
        givenFindByIdResponds();

        UUID institutionId = UUID.randomUUID();
        UUID schoolGradeId = UUID.randomUUID();

        service.handle(new UpdateLearnerCommand(existing.getId(), NATURAL_PERSON_ID, FAMILY_GROUP_ID,
                institutionId, schoolGradeId, true, ScholarshipCondition.PARTIAL, 2027,
                "Maria da Silva", "MAT-2027-0001", List.of()));

        ArgumentCaptor<Learner> captor = ArgumentCaptor.forClass(Learner.class);
        verify(repository).save(captor.capture());

        assertEquals(existing.getId(), captor.getValue().getId());
        assertEquals(institutionId, captor.getValue().getInstitutionId());
        assertEquals(schoolGradeId, captor.getValue().getSchoolGradeId());
        assertTrue(captor.getValue().isInterestArts());
        assertEquals(ScholarshipCondition.PARTIAL, captor.getValue().getScholarshipCondition());
        assertEquals(2027, captor.getValue().getAcademicYear());
        assertEquals("Maria da Silva", captor.getValue().getMotherName());
        assertEquals("MAT-2027-0001", captor.getValue().getSchoolEnrollment());
    }

    @Test
    @DisplayName("Deve sincronizar os responsáveis informados na edição")
    void handle_ShouldSyncResponsibles() {
        Learner existing = givenLearner();
        when(repository.findByIdOrThrow(existing.getId())).thenReturn(existing);
        when(repository.save(any(Learner.class))).thenAnswer(i -> i.getArgument(0));
        givenFindByIdResponds();

        List<ResponsibleCommand> responsibleList = List.of(
                new ResponsibleCommand(RESPONSIBLE_PERSON_ID, ResponsibleType.FINANCIAL, Kinship.FATHER, null));

        service.handle(givenCommand(responsibleList).withId(existing.getId()));

        verify(familyGroupRepository).existsNaturalPersonMember(FAMILY_GROUP_ID, RESPONSIBLE_PERSON_ID);
    }

    @Test
    @DisplayName("Deve impedir que o educando seja transferido para outra pessoa física")
    void handle_ShouldFailWhenNaturalPersonChanges() {
        Learner existing = givenLearner();
        when(repository.findByIdOrThrow(existing.getId())).thenReturn(existing);

        UpdateLearnerCommand command = new UpdateLearnerCommand(existing.getId(), UUID.randomUUID(), FAMILY_GROUP_ID,
                null, null, false, null, 2026, null, null, List.of());

        InternalException exception = assertThrows(InternalException.class, () -> service.handle(command));

        assertEquals("O educando não pode ser transferido para outra pessoa física.", exception.getMessage());
        verify(repository, never()).save(any());
        verifyNoInteractions(familyGroupRepository, naturalPersonRepository);
    }

    @Test
    @DisplayName("Deve lançar exceção quando o educando não existe")
    void handle_ShouldFailWhenNotFound() {
        UUID id = UUID.randomUUID();
        when(repository.findByIdOrThrow(id))
                .thenThrow(new ResourceNotFoundException("Educando não encontrado com ID: " + id));

        assertThrows(ResourceNotFoundException.class,
                () -> service.handle(givenCommand(List.of()).withId(id)));

        verify(repository, never()).save(any());
    }

    @Test
    @DisplayName("Deve validar o vínculo com o grupo familiar antes de salvar")
    void handle_ShouldValidateFamilyGroupMembership() {
        Learner existing = givenLearner();
        when(repository.findByIdOrThrow(existing.getId())).thenReturn(existing);
        givenNotMemberOfFamilyGroup(NATURAL_PERSON_ID);

        InternalException exception = assertThrows(InternalException.class,
                () -> service.handle(givenCommand(List.of()).withId(existing.getId())));

        assertEquals("A pessoa física informada não pertence a este grupo familiar.", exception.getMessage());
        verify(repository, never()).save(any());
    }

    @Test
    @DisplayName("Não deve salvar quando a sincronização de responsáveis é recusada pelo domínio")
    void handle_ShouldNotSaveWhenResponsibleSyncIsRejected() {
        Learner existing = givenLearner();
        when(repository.findByIdOrThrow(existing.getId())).thenReturn(existing);


        // O próprio educando vem na lista de responsáveis, que o domínio recusa.
        List<ResponsibleCommand> forbidden = List.of(
                new ResponsibleCommand(NATURAL_PERSON_ID, ResponsibleType.FINANCIAL, Kinship.MOTHER, null));

        assertThrows(InternalException.class,
                () -> service.handle(givenCommand(forbidden).withId(existing.getId())));

        verify(repository, never()).save(any());
    }

    @Test
    @DisplayName("Deve buscar pelo id da rota e não pelo id do corpo")
    void handle_ShouldUseIdFromRoute() {
        Learner existing = givenLearner();
        when(repository.findByIdOrThrow(existing.getId())).thenReturn(existing);
        when(repository.save(any(Learner.class))).thenAnswer(i -> i.getArgument(0));
        givenFindByIdResponds();

        service.handle(givenCommand(List.of()).withId(existing.getId()));

        verify(repository).findByIdOrThrow(existing.getId());
    }

    @Test
    @DisplayName("Deve fazer o agregado esquecer o papel do responsável que saiu da lista")
    void handle_ShouldForgetRoleOfRemovedResponsible() {
        Learner existing = givenLearner();
        existing.syncResponsibleList(List.of(
                Responsible.create(existing, RESPONSIBLE_PERSON_ID, ResponsibleType.FINANCIAL, Kinship.MOTHER, null)));

        when(repository.findByIdOrThrow(existing.getId())).thenReturn(existing);
        when(repository.save(any(Learner.class))).thenAnswer(i -> i.getArgument(0));
        givenFindByIdResponds();

        service.handle(new UpdateLearnerCommand(existing.getId(), NATURAL_PERSON_ID, FAMILY_GROUP_ID,
                null, null, false, null, 2026, null, null, List.of()));

        ArgumentCaptor<List<Responsible>> captor = ArgumentCaptor.captor();
        verify(roleSynchronizer).forgetRemoved(captor.capture());

        assertEquals(1, captor.getValue().size());
        assertEquals(RESPONSIBLE_PERSON_ID, captor.getValue().get(0).getNaturalPersonId());
    }

    @Test
    @DisplayName("Deve declarar o papel do educando e o dos responsáveis que ficaram")
    void handle_ShouldDeclareRolesOfLearnerAndRemainingResponsibles() {

        Learner existing = givenLearner();
        existing.syncResponsibleList(List.of(
                Responsible.create(existing, RESPONSIBLE_PERSON_ID, ResponsibleType.FINANCIAL, Kinship.MOTHER, null)));

        when(repository.findByIdOrThrow(existing.getId())).thenReturn(existing);
        when(repository.save(any(Learner.class))).thenAnswer(i -> i.getArgument(0));
        givenFindByIdResponds();

        service.handle(new UpdateLearnerCommand(existing.getId(), NATURAL_PERSON_ID, FAMILY_GROUP_ID,
                null, null, false, null, 2026, null, null, List.of(
                new ResponsibleCommand(RESPONSIBLE_PERSON_ID, ResponsibleType.FINANCIAL, Kinship.MOTHER, null))));

        ArgumentCaptor<Learner> captor = ArgumentCaptor.forClass(Learner.class);
        verify(roleSynchronizer).declare(captor.capture());

        assertEquals(2, captor.getValue().roleAssignments().size());
    }

    @Test
    @DisplayName("Deve revogar o papel do educando e re-declarar, para a tag não ficar órfã")
    void handle_ShouldForgetBeforeDeclaringRoles() {
        Learner existing = givenLearner();
        when(repository.findByIdOrThrow(existing.getId())).thenReturn(existing);
        when(repository.save(any(Learner.class))).thenAnswer(i -> i.getArgument(0));
        givenFindByIdResponds();

        service.handle(new UpdateLearnerCommand(existing.getId(), NATURAL_PERSON_ID, FAMILY_GROUP_ID,
                null, null, false, null, 2026, null, null, List.of()));

        verify(roleSynchronizer).forgetRemoved(List.of());

        InOrder inOrder = inOrder(roleSynchronizer);
        inOrder.verify(roleSynchronizer).forgetRemoved(any());
        inOrder.verify(roleSynchronizer).declare(any());
    }
}
