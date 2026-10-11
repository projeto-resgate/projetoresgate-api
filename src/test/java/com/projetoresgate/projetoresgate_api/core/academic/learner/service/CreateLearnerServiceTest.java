package com.projetoresgate.projetoresgate_api.core.academic.learner.service;

import com.projetoresgate.projetoresgate_api.core.academic.learner.usecase.command.ResponsibleCommand;
import com.projetoresgate.projetoresgate_api.core.academic.learner.api.dto.LearnerResponse;
import com.projetoresgate.projetoresgate_api.core.academic.learner.domain.Learner;
import com.projetoresgate.projetoresgate_api.core.academic.learner.domain.Responsible;
import com.projetoresgate.projetoresgate_api.core.academic.learner.domain.enums.Kinship;
import com.projetoresgate.projetoresgate_api.core.academic.learner.domain.enums.LearnerStatus;
import com.projetoresgate.projetoresgate_api.core.academic.learner.domain.enums.ResponsibleType;
import com.projetoresgate.projetoresgate_api.core.academic.learner.domain.enums.ScholarshipCondition;
import com.projetoresgate.projetoresgate_api.core.academic.learner.repository.LearnerRepository;
import com.projetoresgate.projetoresgate_api.core.academic.learner.usecase.FindLearnerByIdUseCase;
import com.projetoresgate.projetoresgate_api.core.academic.learner.usecase.command.CreateLearnerCommand;
import com.projetoresgate.projetoresgate_api.core.academic.learner.usecase.query.FindLearnerByIdQuery;
import com.projetoresgate.projetoresgate_api.core.identity.naturalperson.domain.enums.NaturalPersonRoleType;
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
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("CreateLearnerService - Test")
class CreateLearnerServiceTest {

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
    private LearnerRoleSynchronizer roleSynchronizer;

    @BeforeEach
    void givenPersonBelongsToFamilyGroup() {
        lenient().when(familyGroupRepository.existsNaturalPersonMember(any(UUID.class), any(UUID.class)))
                .thenReturn(true);
    }

    private Set<RoleAssignment> declaredAssignments() {
        ArgumentCaptor<Learner> captor = ArgumentCaptor.forClass(Learner.class);
        verify(roleSynchronizer).declare(captor.capture());

        return captor.getValue().roleAssignments().stream()
                .map(holder -> new RoleAssignment(holder.naturalPersonId(), holder.role()))
                .collect(Collectors.toSet());
    }

    private record RoleAssignment(UUID naturalPersonId, NaturalPersonRoleType role) {
    }

    private void givenNotMemberOfFamilyGroup(UUID naturalPersonId) {
        when(familyGroupRepository.existsNaturalPersonMember(FAMILY_GROUP_ID, naturalPersonId)).thenReturn(false);
    }

    @InjectMocks
    private CreateLearnerService service;

    private static CreateLearnerCommand givenCommand(List<ResponsibleCommand> responsibleList) {
        return new CreateLearnerCommand(
                NATURAL_PERSON_ID,
                FAMILY_GROUP_ID,
                null,
                null,
                false,
                null,
                2026,
                null,
                null,
                responsibleList);
    }

    @Test
    @DisplayName("Deve criar o educando com os dados escolares informados")
    void handle_ShouldCreateLearner() {
        LearnerResponse expected = mockResponse();
        when(repository.save(any(Learner.class))).thenAnswer(i -> i.getArgument(0));
        when(findLearnerByIdUseCase.handle(any(FindLearnerByIdQuery.class))).thenReturn(expected);

        LearnerResponse result = service.handle(givenCommand(List.of()));

        ArgumentCaptor<Learner> captor = ArgumentCaptor.forClass(Learner.class);
        verify(repository).save(captor.capture());

        assertEquals(NATURAL_PERSON_ID, captor.getValue().getNaturalPersonId());
        assertEquals(2026, captor.getValue().getAcademicYear());
        assertSame(expected, result);
    }

    @Test
    @DisplayName("Deve checar a filiação de cada responsável informado")
    void handle_ShouldBuildResponsiblesThroughValidator() {
        when(repository.save(any(Learner.class))).thenAnswer(i -> i.getArgument(0));
        when(findLearnerByIdUseCase.handle(any(FindLearnerByIdQuery.class))).thenReturn(mockResponse());

        List<ResponsibleCommand> responsibleList = List.of(
                new ResponsibleCommand(RESPONSIBLE_PERSON_ID, ResponsibleType.FINANCIAL, Kinship.MOTHER, null));

        service.handle(givenCommand(responsibleList));

        verify(familyGroupRepository).existsNaturalPersonMember(FAMILY_GROUP_ID, RESPONSIBLE_PERSON_ID);
    }

    @Test
    @DisplayName("Deve validar que a pessoa física pertence ao grupo familiar antes de salvar")
    void handle_ShouldValidateFamilyGroupMembershipFirst() {
        givenNotMemberOfFamilyGroup(NATURAL_PERSON_ID);

        InternalException exception = assertThrows(InternalException.class,
                () -> service.handle(givenCommand(List.of())));

        assertEquals("A pessoa física informada não pertence a este grupo familiar.", exception.getMessage());
        verify(repository, never()).save(any());
    }

    @Test
    @DisplayName("Deve recusar com 409 quando a pessoa física já é educando")
    void handle_ShouldFailWhenPersonIsAlreadyLearner() {
        when(repository.existsByNaturalPersonId(NATURAL_PERSON_ID)).thenReturn(true);

        IllegalStateException exception = assertThrows(IllegalStateException.class,
                () -> service.handle(givenCommand(List.of())));

        assertEquals("Esta pessoa física já está cadastrada como educando.", exception.getMessage());
        verify(repository, never()).save(any());
    }

    @Test
    @DisplayName("Deve exigir que a instituição e a série escolar informadas existam")
    void handle_ShouldValidateReferences() {
        UUID institutionId = UUID.randomUUID();
        UUID schoolGradeId = UUID.randomUUID();
        when(repository.save(any(Learner.class))).thenAnswer(i -> i.getArgument(0));
        when(findLearnerByIdUseCase.handle(any(FindLearnerByIdQuery.class))).thenReturn(mockResponse());

        CreateLearnerCommand command = new CreateLearnerCommand(NATURAL_PERSON_ID, FAMILY_GROUP_ID,
                institutionId, schoolGradeId, true, ScholarshipCondition.FULL, 2027,
                "Maria da Silva", "MAT-2027-0001", List.of());


        service.handle(command);

        verify(legalPersonRepository).findByIdOrThrow(institutionId);
        verify(schoolGradeRepository).findByIdOrThrow(schoolGradeId);
    }

    @Test
    @DisplayName("Deve tratar interestArts ausente como falso em vez de nulo")
    void handle_ShouldTreatMissingInterestArtsAsFalse() {
        when(repository.save(any(Learner.class))).thenAnswer(i -> i.getArgument(0));
        when(findLearnerByIdUseCase.handle(any(FindLearnerByIdQuery.class))).thenReturn(mockResponse());

        service.handle(givenCommand(List.of()));

        ArgumentCaptor<Learner> captor = ArgumentCaptor.forClass(Learner.class);
        verify(repository).save(captor.capture());
        assertFalse(captor.getValue().isInterestArts());
    }

    @Test
    @DisplayName("Deve devolver a resposta lida pelo caso de uso de busca pelo id do educando salvo")
    void handle_ShouldReturnFindByIdResponseForSavedLearner() {
        when(repository.save(any(Learner.class))).thenAnswer(i -> i.getArgument(0));

        LearnerResponse expected = new LearnerResponse(UUID.randomUUID(), NATURAL_PERSON_ID, null, null,
                false, null, 2026, null, null, LearnerStatus.ACTIVE, List.of(), null);
        when(findLearnerByIdUseCase.handle(any(FindLearnerByIdQuery.class))).thenReturn(expected);

        LearnerResponse result = service.handle(givenCommand(List.of()));

        assertSame(expected, result);

        ArgumentCaptor<Learner> learnerCaptor = ArgumentCaptor.forClass(Learner.class);
        verify(repository).save(learnerCaptor.capture());

        ArgumentCaptor<FindLearnerByIdQuery> queryCaptor = ArgumentCaptor.forClass(FindLearnerByIdQuery.class);
        verify(findLearnerByIdUseCase).handle(queryCaptor.capture());

        assertEquals(learnerCaptor.getValue().getId(), queryCaptor.getValue().id());
    }

    private LearnerResponse mockResponse() {
        return new LearnerResponse(UUID.randomUUID(), NATURAL_PERSON_ID, null, null,
                false, null, 2026, null, null, LearnerStatus.ACTIVE, List.of(), null);
    }

    @Test
    @DisplayName("Deve declarar o papel do educando e o de cada responsável informado")
    void handle_ShouldDeclareRolesOfLearnerAndResponsibles() {
        when(repository.save(any(Learner.class))).thenAnswer(i -> i.getArgument(0));
        when(findLearnerByIdUseCase.handle(any(FindLearnerByIdQuery.class))).thenReturn(mockResponse());

        service.handle(givenCommand(List.of(
                new ResponsibleCommand(RESPONSIBLE_PERSON_ID, ResponsibleType.FINANCIAL, Kinship.MOTHER, null))));

        assertEquals(Set.of(
                        new RoleAssignment(NATURAL_PERSON_ID, NaturalPersonRoleType.LEARNER),
                        new RoleAssignment(RESPONSIBLE_PERSON_ID, NaturalPersonRoleType.RESPONSIBLE)),
                declaredAssignments());
    }

    @Test
    @DisplayName("Deve declarar apenas o papel do educando quando não houver responsáveis")
    void handle_ShouldDeclareOnlyLearnerRoleWhenNoResponsibles() {
        when(repository.save(any(Learner.class))).thenAnswer(i -> i.getArgument(0));
        when(findLearnerByIdUseCase.handle(any(FindLearnerByIdQuery.class))).thenReturn(mockResponse());

        service.handle(givenCommand(List.of()));

        assertEquals(Set.of(new RoleAssignment(NATURAL_PERSON_ID, NaturalPersonRoleType.LEARNER)),
                declaredAssignments());
    }

    @Test
    @DisplayName("Não deve revogar papel na criação")
    void handle_ShouldNotForgetRolesOnCreate() {
        when(repository.save(any(Learner.class))).thenAnswer(i -> i.getArgument(0));
        when(findLearnerByIdUseCase.handle(any(FindLearnerByIdQuery.class))).thenReturn(mockResponse());

        service.handle(givenCommand(List.of()));

        verify(roleSynchronizer, never()).forgetRemoved(any());
    }

    @Test
    @DisplayName("Deve propagar 404 quando o grupo familiar não existe")
    void handle_ShouldFailWhenFamilyGroupNotFound() {
        UUID familyGroupId = UUID.randomUUID();
        when(familyGroupRepository.findByIdOrThrow(familyGroupId))
                .thenThrow(new ResourceNotFoundException("Grupo familiar não encontrado com ID: " + familyGroupId));

        CreateLearnerCommand command = new CreateLearnerCommand(NATURAL_PERSON_ID, familyGroupId,
                null, null, false, null, 2026, null, null, List.of());

        assertThrows(ResourceNotFoundException.class, () -> service.handle(command));

        verify(repository, never()).save(any());
    }

    @Test
    @DisplayName("Deve propagar 404 quando a pessoa física do educando não existe")
    void handle_ShouldFailWhenNaturalPersonNotFound() {
        when(naturalPersonRepository.findByIdOrThrow(NATURAL_PERSON_ID))
                .thenThrow(new ResourceNotFoundException("Pessoa física não encontrada com ID: " + NATURAL_PERSON_ID));

        assertThrows(ResourceNotFoundException.class, () -> service.handle(givenCommand(List.of())));

        verify(repository, never()).save(any());
    }

    @Test
    @DisplayName("Deve recusar responsável que não pertence ao grupo familiar do educando")
    void handle_ShouldRejectResponsibleOutsideFamilyGroup() {
        givenNotMemberOfFamilyGroup(RESPONSIBLE_PERSON_ID);

        List<ResponsibleCommand> responsibles = List.of(
                new ResponsibleCommand(RESPONSIBLE_PERSON_ID, ResponsibleType.FINANCIAL, Kinship.MOTHER, null));

        InternalException exception = assertThrows(InternalException.class,
                () -> service.handle(givenCommand(responsibles)));

        assertEquals("A pessoa física informada não pertence a este grupo familiar.", exception.getMessage());
        verify(repository, never()).save(any());
    }

    @Test
    @DisplayName("Deve exigir que a série escolar do responsável exista")
    void handle_ShouldRequireExistingResponsibleSchoolGrade() {
        UUID responsibleSchoolGradeId = UUID.randomUUID();
        when(schoolGradeRepository.findByIdOrThrow(responsibleSchoolGradeId))
                .thenThrow(new ResourceNotFoundException("Série escolar não encontrada com ID: "
                        + responsibleSchoolGradeId));

        List<ResponsibleCommand> responsibles = List.of(
                new ResponsibleCommand(RESPONSIBLE_PERSON_ID, ResponsibleType.FINANCIAL, Kinship.MOTHER,
                        responsibleSchoolGradeId));

        assertThrows(ResourceNotFoundException.class, () -> service.handle(givenCommand(responsibles)));

        verify(repository, never()).save(any());
    }

    @Test
    @DisplayName("Deve vincular cada responsável ao educando criado")
    void handle_ShouldBuildResponsiblesBoundToLearner() {
        when(repository.save(any(Learner.class))).thenAnswer(i -> i.getArgument(0));
        when(findLearnerByIdUseCase.handle(any(FindLearnerByIdQuery.class))).thenReturn(mockResponse());

        List<ResponsibleCommand> responsibles = List.of(
                new ResponsibleCommand(RESPONSIBLE_PERSON_ID, ResponsibleType.FINANCIAL, Kinship.MOTHER, null));

        service.handle(givenCommand(responsibles));

        ArgumentCaptor<Learner> captor = ArgumentCaptor.forClass(Learner.class);
        verify(repository).save(captor.capture());

        Learner saved = captor.getValue();
        Responsible responsible = saved.getResponsibleList().get(0);

        assertEquals(1, saved.getResponsibleList().size());
        assertEquals(RESPONSIBLE_PERSON_ID, responsible.getNaturalPersonId());
        assertEquals(ResponsibleType.FINANCIAL, responsible.getType());
        assertEquals(Kinship.MOTHER, responsible.getKinship());
        assertSame(saved, responsible.getLearner());
    }

    @Test
    @DisplayName("Não deve consultar as referências de instituição e série quando elas não são informadas")
    void handle_ShouldSkipNullReferences() {
        when(repository.save(any(Learner.class))).thenAnswer(i -> i.getArgument(0));
        when(findLearnerByIdUseCase.handle(any(FindLearnerByIdQuery.class))).thenReturn(mockResponse());

        service.handle(givenCommand(List.of()));

        verifyNoInteractions(legalPersonRepository, schoolGradeRepository);
    }
}
