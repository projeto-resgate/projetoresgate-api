package com.projetoresgate.projetoresgate_api.core.academic.learner.domain;

import com.projetoresgate.projetoresgate_api.core.academic.learner.domain.enums.Kinship;
import com.projetoresgate.projetoresgate_api.core.academic.learner.domain.enums.LearnerStatus;
import com.projetoresgate.projetoresgate_api.core.academic.learner.domain.enums.LearnerStatusEventType;
import com.projetoresgate.projetoresgate_api.core.academic.learner.domain.enums.ResponsibleType;
import com.projetoresgate.projetoresgate_api.core.academic.learner.domain.enums.ScholarshipCondition;
import com.projetoresgate.projetoresgate_api.infrastructure.exception.InternalException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Learner - Test")
class LearnerTest {

    private static final UUID LEARNER_PERSON_ID = UUID.randomUUID();
    private static final UUID OTHER_PERSON_ID = UUID.randomUUID();

    private static Learner newLearner() {
        return Learner.create(LEARNER_PERSON_ID, null, null, false, null, 2026, null, null);
    }

    private static Responsible newResponsible(Learner learner, UUID naturalPersonId) {
        return Responsible.create(learner, naturalPersonId, ResponsibleType.FINANCIAL, Kinship.MOTHER, null);
    }

    @Test
    @DisplayName("Deve criar o educando com id gerado e os dados escolares informados")
    void create_ShouldBuildLearner() {
        UUID institutionId = UUID.randomUUID();
        UUID schoolGradeId = UUID.randomUUID();

        Learner learner = Learner.create(LEARNER_PERSON_ID, institutionId, schoolGradeId, true,
                ScholarshipCondition.PARTIAL, 2026, "Maria da Silva", "MAT-2026-0001");

        assertNotNull(learner.getId());
        assertEquals(LEARNER_PERSON_ID, learner.getNaturalPersonId());
        assertEquals(institutionId, learner.getInstitutionId());
        assertEquals(schoolGradeId, learner.getSchoolGradeId());
        assertTrue(learner.isInterestArts());
        assertEquals(ScholarshipCondition.PARTIAL, learner.getScholarshipCondition());
        assertEquals(2026, learner.getAcademicYear());
        assertEquals("Maria da Silva", learner.getMotherName());
        assertEquals("MAT-2026-0001", learner.getSchoolEnrollment());
        assertTrue(learner.getResponsibleList().isEmpty());
    }

    @Test
    @DisplayName("Deve exigir a pessoa física do educando")
    void create_ShouldFailWithoutNaturalPerson() {
        InternalException exception = assertThrows(InternalException.class,
                () -> Learner.create(null, null, null, false, null, 2026, null, null));

        assertEquals("A pessoa física do educando é obrigatória.", exception.getMessage());
    }

    @Test
    @DisplayName("Deve recusar ano letivo fora da faixa aceita")
    void create_ShouldFailWhenAcademicYearIsOutOfRange() {
        InternalException exception = assertThrows(InternalException.class,
                () -> Learner.create(LEARNER_PERSON_ID, null, null, false, null, 1899, null, null));

        assertEquals("O ano letivo deve estar entre 1900 e 2100.", exception.getMessage());
    }

    @Test
    @DisplayName("Deve recusar nome da mãe e matrícula acima do limite")
    void create_ShouldFailWhenTextFieldsExceedLimit() {
        String tooLong = "a".repeat(256);

        InternalException motherException = assertThrows(InternalException.class,
                () -> Learner.create(LEARNER_PERSON_ID, null, null, false, null, 2026, tooLong, null));
        InternalException enrollmentException = assertThrows(InternalException.class,
                () -> Learner.create(LEARNER_PERSON_ID, null, null, false, null, 2026, null, tooLong));

        assertEquals("O nome da mãe não pode exceder 255 caracteres.", motherException.getMessage());
        assertEquals("A matrícula escolar não pode exceder 255 caracteres.", enrollmentException.getMessage());
    }

    @Test
    @DisplayName("Deve impedir que o educando seja responsável de si mesmo")
    void syncResponsibleList_ShouldFailWhenLearnerIsItsOwnResponsible() {
        Learner learner = newLearner();

        InternalException exception = assertThrows(InternalException.class,
                () -> learner.syncResponsibleList(List.of(newResponsible(learner, LEARNER_PERSON_ID))));

        assertEquals("A pessoa física do educando não pode ser responsável de si mesma.", exception.getMessage());
        assertTrue(learner.getResponsibleList().isEmpty());
    }

    @Test
    @DisplayName("Deve impedir que a mesma pessoa física entre duas vezes como responsável")
    void syncResponsibleList_ShouldFailWhenResponsibleIsDuplicated() {
        Learner learner = newLearner();

        InternalException exception = assertThrows(InternalException.class,
                () -> learner.syncResponsibleList(List.of(
                        newResponsible(learner, OTHER_PERSON_ID),
                        newResponsible(learner, OTHER_PERSON_ID))));

        assertEquals("A mesma pessoa física não pode ser responsável do educando mais de uma vez.",
                exception.getMessage());
    }

    @Test
    @DisplayName("Deve atualizar o responsável já vinculado em vez de duplicá-lo")
    void syncResponsibleList_ShouldUpdateExistingResponsible() {
        Learner learner = newLearner();
        Responsible responsible = newResponsible(learner, OTHER_PERSON_ID);
        learner.syncResponsibleList(List.of(responsible));

        UUID responsibleId = responsible.getId();

        learner.syncResponsibleList(List.of(
                Responsible.create(learner, OTHER_PERSON_ID, ResponsibleType.COMMUNICATION,
                        Kinship.FATHER, null)));

        assertEquals(1, learner.getResponsibleList().size());
        assertEquals(responsibleId, learner.getResponsibleList().get(0).getId());
        assertEquals(ResponsibleType.COMMUNICATION, learner.getResponsibleList().get(0).getType());
        assertEquals(Kinship.FATHER, learner.getResponsibleList().get(0).getKinship());
    }

    @Test
    @DisplayName("Deve remover da coleção o responsável que não vier na lista")
    void syncResponsibleList_ShouldRemoveResponsibleLeftOut() {
        Learner learner = newLearner();
        Responsible kept = newResponsible(learner, OTHER_PERSON_ID);
        Responsible removed = newResponsible(learner, UUID.randomUUID());
        learner.syncResponsibleList(List.of(kept, removed));

        learner.syncResponsibleList(List.of(kept));

        assertEquals(1, learner.getResponsibleList().size());
        assertEquals(kept.getId(), learner.getResponsibleList().get(0).getId());
    }

    @Test
    @DisplayName("Deve esvaziar a lista quando a sincronização vem sem responsáveis")
    void syncResponsibleList_ShouldClearListWhenNoResponsibleIsSent() {
        Learner learner = newLearner();
        learner.syncResponsibleList(List.of(newResponsible(learner, OTHER_PERSON_ID)));

        learner.syncResponsibleList(List.of());

        assertTrue(learner.getResponsibleList().isEmpty());
    }

    @Test
    @DisplayName("Deve tratar lista nula como lista vazia")
    void syncResponsibleList_ShouldTreatNullAsEmpty() {
        Learner learner = newLearner();

        learner.syncResponsibleList(null);

        assertTrue(learner.getResponsibleList().isEmpty());
    }

    @Test
    @DisplayName("Deve recusar a sincronização que traz o próprio educando como responsável")
    void syncResponsibleList_ShouldFailWhenUpdateBringsTheLearnerAsResponsible() {
        Learner learner = newLearner();
        Responsible responsible = newResponsible(learner, OTHER_PERSON_ID);
        learner.syncResponsibleList(List.of(responsible));

        InternalException exception = assertThrows(InternalException.class,
                () -> learner.syncResponsibleList(List.of(
                        newResponsible(learner, LEARNER_PERSON_ID))));

        assertEquals("A pessoa física do educando não pode ser responsável de si mesma.", exception.getMessage());
    }

    @Test
    @DisplayName("Deve recusar a sincronização inválida sem alterar os responsáveis já vinculados")
    void syncResponsibleList_ShouldKeepCurrentListWhenSyncIsRejected() {
        Learner learner = newLearner();
        Responsible responsible = newResponsible(learner, OTHER_PERSON_ID);
        learner.syncResponsibleList(List.of(responsible));

        assertThrows(InternalException.class,
                () -> learner.syncResponsibleList(List.of(
                        newResponsible(learner, OTHER_PERSON_ID),
                        newResponsible(learner, LEARNER_PERSON_ID))));

        assertEquals(1, learner.getResponsibleList().size());
        assertEquals(OTHER_PERSON_ID, learner.getResponsibleList().get(0).getNaturalPersonId());
        assertEquals(ResponsibleType.FINANCIAL, learner.getResponsibleList().get(0).getType());
    }

    @Test
    @DisplayName("Deve aplicar os dados escolares pela cadeia de atualização")
    void update_ShouldApplySchoolData() {
        Learner learner = newLearner();
        UUID institutionId = UUID.randomUUID();
        UUID schoolGradeId = UUID.randomUUID();

        learner.update()
                .institutionId(institutionId)
                .schoolGradeId(schoolGradeId)
                .interestArts(true)
                .scholarshipCondition(ScholarshipCondition.FULL)
                .academicYear(2027)
                .motherName("Maria da Silva")
                .schoolEnrollment("MAT-2027-0001")
                .apply();

        assertEquals(institutionId, learner.getInstitutionId());
        assertEquals(schoolGradeId, learner.getSchoolGradeId());
        assertTrue(learner.isInterestArts());
        assertEquals(ScholarshipCondition.FULL, learner.getScholarshipCondition());
        assertEquals(2027, learner.getAcademicYear());
        assertEquals("Maria da Silva", learner.getMotherName());
        assertEquals("MAT-2027-0001", learner.getSchoolEnrollment());
    }

    @Test
    @DisplayName("Deve recusar a atualização que deixa o ano letivo inválido")
    void update_ShouldRejectInvalidAcademicYear() {
        Learner learner = newLearner();

        assertThrows(InternalException.class, () -> learner.update().academicYear(0).apply());
    }

    @Test
    @DisplayName("Deve criar o educando ativo e sem histórico de status")
    void create_ShouldStartActiveWithEmptyHistory() {
        Learner learner = newLearner();

        assertEquals(LearnerStatus.ACTIVE, learner.getStatus());
        assertTrue(learner.getStatusHistory().isEmpty());
    }

    @Test
    @DisplayName("Deve marcar como desistente e guardar motivo e data informados")
    void dropOut_ShouldSetStatusAndStoreHistory() {
        Learner learner = newLearner();
        LocalDate eventDate = LocalDate.of(2026, 5, 10);

        learner.dropOut("Mudou de cidade", eventDate);

        assertEquals(LearnerStatus.DROPPED_OUT, learner.getStatus());
        assertEquals(1, learner.getStatusHistory().size());
        assertEquals(LearnerStatusEventType.DROPOUT, learner.getStatusHistory().get(0).getType());
        assertEquals("Mudou de cidade", learner.getStatusHistory().get(0).getReason());
        assertEquals(eventDate, learner.getStatusHistory().get(0).getEventDate());
    }

    @Test
    @DisplayName("Deve usar a data de hoje quando a desistência não informar data")
    void dropOut_ShouldDefaultEventDateToToday() {
        Learner learner = newLearner();

        learner.dropOut("Sem data informada", null);

        assertEquals(LocalDate.now(), learner.getStatusHistory().get(0).getEventDate());
    }

    @Test
    @DisplayName("Deve recusar desistência sem motivo e manter o educando ativo")
    void dropOut_ShouldFailWithoutReasonAndKeepStatus() {
        Learner learner = newLearner();

        assertThrows(InternalException.class, () -> learner.dropOut("   ", null));

        assertEquals(LearnerStatus.ACTIVE, learner.getStatus());
        assertTrue(learner.getStatusHistory().isEmpty());
    }

    @Test
    @DisplayName("Deve recusar nova desistência de um educando já desistente")
    void dropOut_ShouldFailWhenAlreadyDropped() {
        Learner learner = newLearner();
        learner.dropOut("Primeira desistência", LocalDate.of(2026, 5, 10));

        IllegalStateException exception = assertThrows(IllegalStateException.class,
                () -> learner.dropOut("Segunda desistência", null));

        assertEquals("O educando já está desistente.", exception.getMessage());
        assertEquals(1, learner.getStatusHistory().size());
    }

    @Test
    @DisplayName("Deve reativar o educando e registrar a reativação no histórico")
    void reactivate_ShouldSetActiveAndStoreHistory() {
        Learner learner = newLearner();
        learner.dropOut("Mudou de cidade", LocalDate.of(2026, 5, 10));

        learner.reactivate("Retornou no próximo ano letivo", LocalDate.of(2027, 2, 1));

        assertEquals(LearnerStatus.ACTIVE, learner.getStatus());
        assertEquals(2, learner.getStatusHistory().size());
        assertEquals(LearnerStatusEventType.REACTIVATION, learner.getStatusHistory().get(1).getType());
        assertEquals("Retornou no próximo ano letivo", learner.getStatusHistory().get(1).getReason());
        assertEquals(LocalDate.of(2027, 2, 1), learner.getStatusHistory().get(1).getEventDate());
    }

    @Test
    @DisplayName("Deve recusar reativação de um educando que não está desistente")
    void reactivate_ShouldFailWhenNotDropped() {
        Learner learner = newLearner();

        IllegalStateException exception = assertThrows(IllegalStateException.class,
                () -> learner.reactivate(null, null));

        assertEquals("O educando não está desistente.", exception.getMessage());
        assertTrue(learner.getStatusHistory().isEmpty());
    }

    @Test
    @DisplayName("Deve manter desistência e reativação no histórico após novo ciclo")
    void statusHistory_ShouldKeepEveryTransition() {
        Learner learner = newLearner();

        learner.dropOut("Primeira", LocalDate.of(2026, 3, 1));
        learner.reactivate("Voltou", LocalDate.of(2026, 8, 1));
        learner.dropOut("Segunda", LocalDate.of(2026, 9, 1));

        assertEquals(LearnerStatus.DROPPED_OUT, learner.getStatus());
        assertEquals(3, learner.getStatusHistory().size());
        assertEquals(LearnerStatusEventType.DROPOUT, learner.getStatusHistory().get(0).getType());
        assertEquals(LearnerStatusEventType.REACTIVATION, learner.getStatusHistory().get(1).getType());
        assertEquals(LearnerStatusEventType.DROPOUT, learner.getStatusHistory().get(2).getType());
    }
}