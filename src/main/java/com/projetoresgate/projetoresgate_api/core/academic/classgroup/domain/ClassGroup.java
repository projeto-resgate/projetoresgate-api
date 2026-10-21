package com.projetoresgate.projetoresgate_api.core.academic.classgroup.domain;

import com.projetoresgate.projetoresgate_api.core.academic.classgroup.domain.enums.WeekDay;
import com.projetoresgate.projetoresgate_api.core.academic.schoolgrade.domain.SchoolGrade;
import com.projetoresgate.projetoresgate_api.infrastructure.exception.InternalException;
import com.projetoresgate.projetoresgate_api.shared.entity.AuditableEntity;
import jakarta.persistence.*;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import static java.util.Objects.nonNull;

@Entity
@Table(name = "class_group")
@SQLDelete(sql = "UPDATE class_group SET deleted_at = now() WHERE id = ?")
@SQLRestriction("deleted_at IS NULL")
public class ClassGroup extends AuditableEntity {

    private static final int TITLE_MAX_LENGTH = 255;
    private static final int NOTE_MAX_LENGTH = 2000;

    @Id
    private UUID id;

    @Column(nullable = false, length = TITLE_MAX_LENGTH)
    private String title;

    @Column(name = "student_projections", nullable = false)
    private Integer studentProjections;

    @Column(name = "start_date", nullable = false)
    private LocalDateTime startDate;

    @Column(name = "end_date", nullable = false)
    private LocalDateTime endDate;

    @Column(columnDefinition = "TEXT")
    private String note;

    @Column(name = "program_id", nullable = false)
    private UUID programId;

    @Column(name = "institution_id", nullable = false)
    private UUID institutionId;

    @Column(nullable = false)
    private boolean active = true;

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(name = "class_school_grade",
            joinColumns = @JoinColumn(name = "class_group_id"),
            inverseJoinColumns = @JoinColumn(name = "school_grade_id"))
    private List<SchoolGrade> schoolGradeList = new ArrayList<>();

    protected ClassGroup() {
    }

    private ClassGroup(UUID id, UUID programId, UUID institutionId, Integer studentProjections,
                       LocalDateTime startDate, Integer durationInMonths, String note, String title,
                       List<SchoolGrade> schoolGradeList) {
        this.id = id;
        this.programId = programId;
        this.institutionId = institutionId;
        this.studentProjections = studentProjections;
        this.startDate = startDate;
        this.endDate = resolveEndDate(startDate, durationInMonths);
        this.note = note;
        this.title = title;
        this.active = true;
        this.schoolGradeList = nonNull(schoolGradeList) ? new ArrayList<>(schoolGradeList) : new ArrayList<>();
        validate();
    }

    public static ClassGroup create(UUID programId, UUID institutionId, Integer studentProjections,
                                    LocalDateTime startDate, Integer durationInMonths, String note,
                                    String programName, List<SchoolGrade> schoolGradeList) {
        return new ClassGroup(UUID.randomUUID(), programId, institutionId, studentProjections, startDate,
                durationInMonths, note, composeTitle(programName, schoolGradeList, List.of()), schoolGradeList);
    }

    private static LocalDateTime resolveEndDate(LocalDateTime startDate, Integer durationInMonths) {
        return nonNull(startDate) && nonNull(durationInMonths) ? startDate.plusMonths(durationInMonths) : null;
    }

    public void validate() {
        if (!StringUtils.hasText(this.title)) {
            throw new InternalException("O título da turma é obrigatório.");
        }
        if (this.title.length() > TITLE_MAX_LENGTH) {
            throw new InternalException("O título da turma não pode exceder 255 caracteres.");
        }
        if (!nonNull(this.programId)) {
            throw new InternalException("O programa da turma é obrigatório.");
        }
        if (!nonNull(this.institutionId)) {
            throw new InternalException("A instituição da turma é obrigatória.");
        }
        if (!nonNull(this.studentProjections)) {
            throw new InternalException("A previsão de alunos é obrigatória.");
        }
        if (this.studentProjections < 0) {
            throw new InternalException("A previsão de alunos não pode ser negativa.");
        }
        if (!nonNull(this.startDate)) {
            throw new InternalException("A data de início é obrigatória.");
        }
        if (!nonNull(this.endDate)) {
            throw new InternalException("A data de término é obrigatória.");
        }
        if (this.endDate.isBefore(this.startDate)) {
            throw new InternalException("A data de término não pode ser anterior à data de início.");
        }
        if (nonNull(this.note) && this.note.length() > NOTE_MAX_LENGTH) {
            throw new InternalException("A observação não pode exceder 2000 caracteres.");
        }
    }

    public void refreshTitle(String programName, List<WeekDay> weekDays) {
        String title = composeTitle(programName, this.schoolGradeList, weekDays);
        if (!StringUtils.hasText(title) || title.length() > TITLE_MAX_LENGTH) {
            throw new InternalException("Não foi possível gerar o título da turma.");
        }
        this.title = title;
    }

    private static String composeTitle(String programName, List<SchoolGrade> schoolGrades, List<WeekDay> weekDays) {
        List<String> parts = new ArrayList<>(3);

        if (StringUtils.hasText(programName)) {
            parts.add(programName.trim());
        }

        String grades = joinSchoolGrades(schoolGrades);
        if (StringUtils.hasText(grades)) {
            parts.add(grades);
        }

        String days = joinWeekDays(weekDays);
        if (StringUtils.hasText(days)) {
            parts.add(days);
        }

        return String.join(" - ", parts);
    }

    private static String joinSchoolGrades(List<SchoolGrade> schoolGrades) {
        if (schoolGrades == null || schoolGrades.isEmpty()) {
            return null;
        }
        return schoolGrades.stream()
                .sorted(Comparator.comparing(SchoolGrade::getGradeOrder))
                .map(ClassGroup::abbreviatedGrade)
                .distinct()
                .collect(Collectors.joining(", "));
    }

    private static String abbreviatedGrade(SchoolGrade schoolGrade) {
        return StringUtils.hasText(schoolGrade.getAbbreviation())
                ? schoolGrade.getAbbreviation().trim()
                : schoolGrade.getName().trim();
    }

    private static String joinWeekDays(List<WeekDay> weekDays) {
        if (weekDays == null || weekDays.isEmpty()) {
            return null;
        }
        return weekDays.stream()
                .distinct()
                .sorted(Comparator.comparingInt(WeekDay::ordinal))
                .map(WeekDay::getAbbreviation)
                .collect(Collectors.joining(", "));
    }

    public void resume() {
        this.active = true;
    }

    public void suspend() {
        this.active = false;
    }

    public Updater update() {
        return new Updater();
    }

    public class Updater {

        private LocalDateTime pendingStartDate;
        private Integer pendingDurationInMonths;

        public Updater programId(UUID programId) {
            if (nonNull(programId)) {
                ClassGroup.this.programId = programId;
            }
            return this;
        }

        public Updater institutionId(UUID institutionId) {
            if (nonNull(institutionId)) {
                ClassGroup.this.institutionId = institutionId;
            }
            return this;
        }

        public Updater studentProjections(Integer studentProjections) {
            if (nonNull(studentProjections)) {
                ClassGroup.this.studentProjections = studentProjections;
            }
            return this;
        }

        public Updater startDate(LocalDateTime startDate) {
            this.pendingStartDate = startDate;
            return this;
        }

        public Updater durationInMonths(Integer durationInMonths) {
            this.pendingDurationInMonths = durationInMonths;
            return this;
        }

        public Updater note(String note) {
            ClassGroup.this.note = note;
            return this;
        }

        public Updater active(Boolean active) {
            if (nonNull(active)) {
                ClassGroup.this.active = active;
            }
            return this;
        }

        public Updater schoolGradeList(List<SchoolGrade> schoolGradeList) {
            ClassGroup.this.schoolGradeList = nonNull(schoolGradeList) ? new ArrayList<>(schoolGradeList)
                    : new ArrayList<>();
            return this;
        }

        public ClassGroup apply() {
            if (nonNull(pendingStartDate)) {
                ClassGroup.this.startDate = pendingStartDate;
            }
            if (nonNull(pendingDurationInMonths)) {
                ClassGroup.this.endDate = ClassGroup.this.startDate.plusMonths(pendingDurationInMonths);
            }
            ClassGroup.this.validate();
            return ClassGroup.this;
        }
    }

    public UUID getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public Integer getStudentProjections() {
        return studentProjections;
    }

    public LocalDateTime getStartDate() {
        return startDate;
    }

    public LocalDateTime getEndDate() {
        return endDate;
    }

    public String getNote() {
        return note;
    }

    public UUID getProgramId() {
        return programId;
    }

    public UUID getInstitutionId() {
        return institutionId;
    }

    public boolean isActive() {
        return active;
    }

    public List<SchoolGrade> getSchoolGradeList() {
        return schoolGradeList;
    }
}
