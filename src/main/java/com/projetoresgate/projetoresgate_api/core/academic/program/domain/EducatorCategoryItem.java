package com.projetoresgate.projetoresgate_api.core.academic.program.domain;

import com.projetoresgate.projetoresgate_api.infrastructure.exception.InternalException;
import com.projetoresgate.projetoresgate_api.shared.entity.AuditableEntity;
import jakarta.persistence.*;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;
import org.springframework.util.StringUtils;

import java.util.UUID;

import static java.util.Objects.nonNull;

@Entity
@Table(name = "educator_category_item")
@SQLDelete(sql = "UPDATE educator_category_item SET deleted_at = now() WHERE id = ?")
@SQLRestriction("deleted_at IS NULL")
public class EducatorCategoryItem extends AuditableEntity {

    @Id
    private UUID id;

    private String name;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "program_id")
    private Program program;

    protected EducatorCategoryItem() {
    }

    private EducatorCategoryItem(UUID id, String name, Program program) {
        this.id = id;
        this.name = name;
        this.program = program;
        validate();
    }

    public static EducatorCategoryItem create(String name, Program program) {
        return new EducatorCategoryItem(UUID.randomUUID(), name, program);
    }

    public void updateName(String name) {
        this.name = name;
        validate();
    }

    public void validate() {
        if (!StringUtils.hasText(this.name)) {
            throw new InternalException("O nome da categoria de educador não pode ser vazio.");
        }
        if (!nonNull(this.program)) {
            throw new InternalException("O programa é obrigatório.");
        }
    }

    public UUID getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public Program getProgram() {
        return program;
    }
}
