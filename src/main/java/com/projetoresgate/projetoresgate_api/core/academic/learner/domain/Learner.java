package com.projetoresgate.projetoresgate_api.core.academic.learner.domain;

import com.projetoresgate.projetoresgate_api.core.academic.learner.domain.enums.LearnerStatus;
import com.projetoresgate.projetoresgate_api.core.academic.learner.domain.enums.LearnerStatusEventType;
import com.projetoresgate.projetoresgate_api.core.academic.learner.domain.enums.ScholarshipCondition;
import com.projetoresgate.projetoresgate_api.core.identity.naturalperson.domain.NaturalPersonRoleHolder;
import com.projetoresgate.projetoresgate_api.core.identity.naturalperson.domain.enums.NaturalPersonRoleType;
import com.projetoresgate.projetoresgate_api.infrastructure.exception.InternalException;
import com.projetoresgate.projetoresgate_api.shared.entity.AuditableEntity;
import jakarta.persistence.*;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;
import org.springframework.util.StringUtils;

import java.time.LocalDate;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

import static java.util.Objects.nonNull;

@Entity
@Table(name = "learner")
@SQLDelete(sql = "UPDATE learner SET deleted_at = now() WHERE id = ?")
@SQLRestriction("deleted_at IS NULL")
public class Learner extends AuditableEntity implements NaturalPersonRoleHolder {

    private static final int NAME_MAX_LENGTH = 255;
    private static final int ENROLLMENT_MAX_LENGTH = 255;
    private static final int EARLIEST_ACADEMIC_YEAR = 1900;
    private static final int LATEST_ACADEMIC_YEAR = 2100;

    @Id
    private UUID id;

    private UUID naturalPersonId;

    private UUID institutionId;

    @Column(name = "school_grade_id")
    private UUID schoolGradeId;

    private boolean interestArts;

    @Enumerated(EnumType.STRING)
    private ScholarshipCondition scholarshipCondition;

    @Column(name = "academic_year")
    private Integer academicYear;

    private String motherName;

    @Column(name = "school_enrollment")
    private String schoolEnrollment;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private LearnerStatus status;

    @OneToMany(mappedBy = "learner", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Responsible> responsibleList = new ArrayList<>();

    @OneToMany(mappedBy = "learner", cascade = CascadeType.ALL)
    private List<LearnerStatusHistory> statusHistory = new ArrayList<>();

    protected Learner() {
    }

    private Learner(UUID id, UUID naturalPersonId, UUID institutionId, UUID schoolGradeId, boolean interestArts,
                    ScholarshipCondition scholarshipCondition, Integer academicYear, String motherName,
                    String schoolEnrollment) {
        this.id = id;
        this.naturalPersonId = naturalPersonId;
        this.institutionId = institutionId;
        this.schoolGradeId = schoolGradeId;
        this.interestArts = interestArts;
        this.scholarshipCondition = scholarshipCondition;
        this.academicYear = academicYear;
        this.motherName = motherName;
        this.schoolEnrollment = schoolEnrollment;
        this.status = LearnerStatus.ACTIVE;
        validate();
    }

    public static Learner create(UUID naturalPersonId, UUID institutionId, UUID schoolGradeId, boolean interestArts,
                                 ScholarshipCondition scholarshipCondition, Integer academicYear,
                                 String motherName, String schoolEnrollment) {
        return new Learner(UUID.randomUUID(), naturalPersonId, institutionId, schoolGradeId, interestArts,
                scholarshipCondition, academicYear, motherName, schoolEnrollment);
    }

    public void validate() {
        if (!nonNull(this.naturalPersonId)) {
            throw new InternalException("A pessoa física do educando é obrigatória.");
        }
        if (!nonNull(this.status)) {
            throw new InternalException("O status do educando é obrigatório.");
        }
        if (StringUtils.hasText(this.motherName) && this.motherName.length() > NAME_MAX_LENGTH) {
            throw new InternalException("O nome da mãe não pode exceder 255 caracteres.");
        }
        if (StringUtils.hasText(this.schoolEnrollment) && this.schoolEnrollment.length() > ENROLLMENT_MAX_LENGTH) {
            throw new InternalException("A matrícula escolar não pode exceder 255 caracteres.");
        }
        validateAcademicYear();
        validateResponsibleList(responsibleList);
    }

    private void validateAcademicYear() {
        if (!nonNull(this.academicYear)) {
            return;
        }
        if (this.academicYear < EARLIEST_ACADEMIC_YEAR || this.academicYear > LATEST_ACADEMIC_YEAR) {
            throw new InternalException("O ano letivo deve estar entre " + EARLIEST_ACADEMIC_YEAR
                    + " e " + LATEST_ACADEMIC_YEAR + ".");
        }
    }

    private void validateResponsibleList(List<Responsible> responsibles) {
        if (responsibles.stream()
                .anyMatch(responsible -> responsible.getNaturalPersonId().equals(this.naturalPersonId))) {
            throw new InternalException("A pessoa física do educando não pode ser responsável de si mesma.");
        }

        long distinctPeople = responsibles.stream()
                .map(Responsible::getNaturalPersonId)
                .distinct()
                .count();

        if (distinctPeople != responsibles.size()) {
            throw new InternalException(
                    "A mesma pessoa física não pode ser responsável do educando mais de uma vez.");
        }
    }

    public List<Responsible> syncResponsibleList(List<Responsible> incoming) {
        List<Responsible> wanted = nonNull(incoming) ? incoming : new ArrayList<>();

        validateResponsibleList(wanted);

        Map<UUID, Responsible> currentByNaturalPerson = responsibleList.stream()
                .collect(Collectors.toMap(Responsible::getNaturalPersonId, Function.identity()));

        List<Responsible> synced = new ArrayList<>(wanted.size());

        for (Responsible responsible : wanted) {
            Responsible current = currentByNaturalPerson.get(responsible.getNaturalPersonId());

            if (nonNull(current)) {
                current.updateFrom(responsible);
                synced.add(current);
            } else {
                synced.add(responsible);
            }
        }

        Set<UUID> keptNaturalPersonIds = wanted.stream()
                .map(Responsible::getNaturalPersonId)
                .collect(Collectors.toSet());

        List<Responsible> removed = responsibleList.stream()
                .filter(responsible -> !keptNaturalPersonIds.contains(responsible.getNaturalPersonId()))
                .toList();

        responsibleList.clear();
        responsibleList.addAll(synced);

        return removed;
    }

    @Override
    public NaturalPersonRoleType role() {
        return NaturalPersonRoleType.LEARNER;
    }

    public List<NaturalPersonRoleHolder> roleAssignments() {
        List<NaturalPersonRoleHolder> assignments = new ArrayList<>(responsibleList.size() + 1);
        assignments.add(this);
        assignments.addAll(responsibleList);

        return assignments;
    }

    public Updater update() {
        return new Updater();
    }

    public void dropOut(String reason, LocalDate eventDate) {
        if (this.status == LearnerStatus.DROPPED_OUT) {
            throw new IllegalStateException("O educando já está desistente.");
        }

        LearnerStatusHistory history = LearnerStatusHistory.create(
                this, LearnerStatusEventType.DROPOUT, reason, resolveEventDate(eventDate));

        this.status = LearnerStatus.DROPPED_OUT;
        this.statusHistory.add(history);
    }

    public void reactivate(String note, LocalDate eventDate) {
        if (this.status == LearnerStatus.ACTIVE) {
            throw new IllegalStateException("O educando não está desistente.");
        }

        LearnerStatusHistory history = LearnerStatusHistory.create(
                this, LearnerStatusEventType.REACTIVATION, note, resolveEventDate(eventDate));

        this.status = LearnerStatus.ACTIVE;
        this.statusHistory.add(history);
    }

    private LocalDate resolveEventDate(LocalDate eventDate) {
        return nonNull(eventDate) ? eventDate : LocalDate.now();
    }

    public class Updater {

        public Updater institutionId(UUID institutionId) {
            Learner.this.institutionId = institutionId;
            return this;
        }

        public Updater schoolGradeId(UUID schoolGradeId) {
            Learner.this.schoolGradeId = schoolGradeId;
            return this;
        }

        public Updater interestArts(boolean interestArts) {
            Learner.this.interestArts = interestArts;
            return this;
        }

        public Updater scholarshipCondition(ScholarshipCondition scholarshipCondition) {
            Learner.this.scholarshipCondition = scholarshipCondition;
            return this;
        }

        public Updater academicYear(Integer academicYear) {
            Learner.this.academicYear = academicYear;
            return this;
        }

        public Updater motherName(String motherName) {
            Learner.this.motherName = motherName;
            return this;
        }

        public Updater schoolEnrollment(String schoolEnrollment) {
            Learner.this.schoolEnrollment = schoolEnrollment;
            return this;
        }

        public Learner apply() {
            Learner.this.validate();
            return Learner.this;
        }
    }

    public UUID getId() {
        return id;
    }

    public UUID getNaturalPersonId() {
        return naturalPersonId;
    }

    @Override
    public UUID naturalPersonId() {
        return naturalPersonId;
    }

    public UUID getInstitutionId() {
        return institutionId;
    }

    public UUID getSchoolGradeId() {
        return schoolGradeId;
    }

    public boolean isInterestArts() {
        return interestArts;
    }

    public ScholarshipCondition getScholarshipCondition() {
        return scholarshipCondition;
    }

    public Integer getAcademicYear() {
        return academicYear;
    }

    public String getMotherName() {
        return motherName;
    }

    public String getSchoolEnrollment() {
        return schoolEnrollment;
    }

    public LearnerStatus getStatus() {
        return status;
    }

    public List<LearnerStatusHistory> getStatusHistory() {
        return statusHistory;
    }

    public List<Responsible> getResponsibleList() {
        return responsibleList;
    }
}