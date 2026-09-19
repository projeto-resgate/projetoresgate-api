package com.projetoresgate.projetoresgate_api.core.academic.program.domain;

import com.projetoresgate.projetoresgate_api.core.academic.program.domain.enums.ProgramStatus;
import com.projetoresgate.projetoresgate_api.infrastructure.exception.InternalException;
import com.projetoresgate.projetoresgate_api.shared.entity.AuditableEntity;
import jakarta.persistence.*;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static java.util.Objects.nonNull;

@Entity
@Table(name = "program")
@SQLDelete(sql = "UPDATE program SET deleted_at = now() WHERE id = ?")
@SQLRestriction("deleted_at IS NULL")
public class Program extends AuditableEntity {

    @Id
    private UUID id;

    private String name;

    private String webSiteUrl;

    @Enumerated(EnumType.STRING)
    private ProgramStatus status;

    private UUID institutionId;

    @OneToMany(mappedBy = "program", cascade = CascadeType.ALL)
    private List<EducatorCategoryItem> educatorCategoryItemList = new ArrayList<>();

    protected Program() {
    }

    private Program(UUID id, String name, String webSiteUrl, ProgramStatus status, UUID institutionId,
                    List<EducatorCategoryItem> educatorCategoryItemList) {
        this.id = id;
        this.name = name;
        this.webSiteUrl = webSiteUrl;
        this.status = status;
        this.institutionId = institutionId;
        this.educatorCategoryItemList = nonNull(educatorCategoryItemList) ? educatorCategoryItemList : new ArrayList<>();
        validate();
    }

    public static Program create(String name, String webSiteUrl, ProgramStatus status, UUID institutionId) {
        return new Program(UUID.randomUUID(), name, webSiteUrl, status, institutionId, new ArrayList<>());
    }

    public void validate() {
        if (!StringUtils.hasText(this.name)) {
            throw new InternalException("O nome do programa não pode ser vazio.");
        }
        if (!nonNull(this.status)) {
            throw new InternalException("O status é obrigatório.");
        }
    }

    public Updater update() {
        return new Updater();
    }

    public class Updater {

        public Updater name(String name) {
            Program.this.name = name;
            return this;
        }

        public Updater webSiteUrl(String webSiteUrl) {
            Program.this.webSiteUrl = webSiteUrl;
            return this;
        }

        public Updater status(ProgramStatus status) {
            Program.this.status = status;
            return this;
        }

        public Program apply() {
            Program.this.validate();
            return Program.this;
        }
    }

    public UUID getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getWebSiteUrl() {
        return webSiteUrl;
    }

    public ProgramStatus getStatus() {
        return status;
    }

    public UUID getInstitutionId() {
        return institutionId;
    }

    public List<EducatorCategoryItem> getEducatorCategoryItemList() {
        return educatorCategoryItemList;
    }
}