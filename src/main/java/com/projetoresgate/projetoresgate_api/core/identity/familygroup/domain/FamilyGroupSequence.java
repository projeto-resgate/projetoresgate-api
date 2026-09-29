package com.projetoresgate.projetoresgate_api.core.identity.familygroup.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.util.Objects;

@Entity
@Table(name = "family_group_sequence")
public class FamilyGroupSequence {

    public static final Long SINGLETON_ID = 1L;

    @Id
    private Long id;

    @Column(name = "current_value", nullable = false)
    private Long currentValue;

    protected FamilyGroupSequence() {
    }

    private FamilyGroupSequence(Long id, Long currentValue) {
        this.id = id;
        this.currentValue = currentValue;
    }

    public static FamilyGroupSequence initial() {
        return new FamilyGroupSequence(SINGLETON_ID, 0L);
    }

    public Long nextValue() {
        this.currentValue = this.currentValue + 1;
        return this.currentValue;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof FamilyGroupSequence other)) return false;
        return Objects.equals(id, other.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    public Long getId() {
        return id;
    }

    public Long getCurrentValue() {
        return currentValue;
    }
}
