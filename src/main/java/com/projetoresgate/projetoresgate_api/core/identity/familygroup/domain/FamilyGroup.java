package com.projetoresgate.projetoresgate_api.core.identity.familygroup.domain;

import com.projetoresgate.projetoresgate_api.core.identity.address.domain.Address;
import com.projetoresgate.projetoresgate_api.core.identity.naturalperson.domain.NaturalPerson;
import com.projetoresgate.projetoresgate_api.infrastructure.exception.InternalException;
import com.projetoresgate.projetoresgate_api.shared.entity.AuditableEntity;
import jakarta.persistence.*;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static java.util.Objects.nonNull;

@Entity
@Table(name = "family_group")
@SQLDelete(sql = "UPDATE family_group SET deleted_at = now() WHERE id = ?")
@SQLRestriction("deleted_at IS NULL")
public class FamilyGroup extends AuditableEntity {

    public static final String FRIENDLY_ID_PREFIX = "FAM-";

    private static final int FRIENDLY_ID_MAX_LENGTH = 20;

    @Id
    private UUID id;

    @Column(name = "friendly_id", nullable = false, updatable = false, length = FRIENDLY_ID_MAX_LENGTH)
    private String friendlyId;

    private String name;

    @Column(precision = 15, scale = 2)
    private BigDecimal householdIncome;

    @Column(precision = 15, scale = 2)
    private BigDecimal perCapitaIncome;

    @Column(precision = 15, scale = 2)
    private BigDecimal educationExpense;

    @Column(precision = 15, scale = 2)
    private BigDecimal healthExpense;

    @Column(precision = 15, scale = 2)
    private BigDecimal housingExpense;

    @Column(name = "number_of_residents")
    private Integer numberOfResidents;

    @OneToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "address_id")
    private Address address;

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(name = "family_group_natural_person",
            joinColumns = @JoinColumn(name = "family_group_id"),
            inverseJoinColumns = @JoinColumn(name = "natural_person_id"))
    private List<NaturalPerson> naturalPersonList = new ArrayList<>();

    protected FamilyGroup() {
    }

    private FamilyGroup(UUID id, String friendlyId, String name, BigDecimal householdIncome, BigDecimal perCapitaIncome,
                        BigDecimal educationExpense, BigDecimal healthExpense, BigDecimal housingExpense,
                        Integer numberOfResidents, Address address, List<NaturalPerson> naturalPersonList) {
        this.id = id;
        this.friendlyId = friendlyId;
        this.name = name;
        this.householdIncome = householdIncome;
        this.perCapitaIncome = perCapitaIncome;
        this.educationExpense = educationExpense;
        this.healthExpense = healthExpense;
        this.housingExpense = housingExpense;
        this.numberOfResidents = numberOfResidents;
        this.address = address;
        this.naturalPersonList = nonNull(naturalPersonList) ? naturalPersonList : new ArrayList<>();
        validate();
    }

    public static FamilyGroup create(String friendlyId, String name, BigDecimal householdIncome,
                                     BigDecimal perCapitaIncome, BigDecimal educationExpense,
                                     BigDecimal healthExpense, BigDecimal housingExpense,
                                     Integer numberOfResidents, Address address) {
        return new FamilyGroup(UUID.randomUUID(), friendlyId, name, householdIncome, perCapitaIncome,
                educationExpense, healthExpense, housingExpense, numberOfResidents, address, new ArrayList<>());
    }

    public void validate() {
        if (!StringUtils.hasText(this.friendlyId)
                || !this.friendlyId.startsWith(FRIENDLY_ID_PREFIX)
                || this.friendlyId.length() > FRIENDLY_ID_MAX_LENGTH) {
            throw new InternalException("O identificador amigável do grupo familiar deve seguir o padrão "
                    + FRIENDLY_ID_PREFIX + "<número>.");
        }
        if (!StringUtils.hasText(this.name)) {
            throw new InternalException("O nome do grupo familiar não pode ser vazio.");
        }
        if (this.name.length() > 255) {
            throw new InternalException("O nome do grupo familiar não pode exceder 255 caracteres.");
        }
        validateNonNegative(this.householdIncome, "A renda familiar");
        validateNonNegative(this.perCapitaIncome, "A renda per capita");
        validateNonNegative(this.educationExpense, "A despesa com educação");
        validateNonNegative(this.healthExpense, "A despesa com saúde");
        validateNonNegative(this.housingExpense, "A despesa com moradia");
        if (nonNull(this.numberOfResidents) && this.numberOfResidents < 1) {
            throw new InternalException("O número de moradores deve ser maior que zero.");
        }
        if (nonNull(this.address)) {
            this.address.validate();
        }
    }

    private void validateNonNegative(BigDecimal value, String field) {
        if (nonNull(value) && value.compareTo(BigDecimal.ZERO) < 0) {
            throw new InternalException(field + " não pode ser negativa.");
        }
    }

    public Updater update() {
        return new Updater();
    }

    public class Updater {

        public Updater name(String name) {
            FamilyGroup.this.name = name;
            return this;
        }

        public Updater householdIncome(BigDecimal householdIncome) {
            FamilyGroup.this.householdIncome = householdIncome;
            return this;
        }

        public Updater perCapitaIncome(BigDecimal perCapitaIncome) {
            FamilyGroup.this.perCapitaIncome = perCapitaIncome;
            return this;
        }

        public Updater educationExpense(BigDecimal educationExpense) {
            FamilyGroup.this.educationExpense = educationExpense;
            return this;
        }

        public Updater healthExpense(BigDecimal healthExpense) {
            FamilyGroup.this.healthExpense = healthExpense;
            return this;
        }

        public Updater housingExpense(BigDecimal housingExpense) {
            FamilyGroup.this.housingExpense = housingExpense;
            return this;
        }

        public Updater numberOfResidents(Integer numberOfResidents) {
            FamilyGroup.this.numberOfResidents = numberOfResidents;
            return this;
        }

        public Updater address(Address address) {
            FamilyGroup.this.address = address;
            return this;
        }

        public Updater naturalPersonList(List<NaturalPerson> naturalPersonList) {
            FamilyGroup.this.naturalPersonList = nonNull(naturalPersonList) ? naturalPersonList : new ArrayList<>();
            return this;
        }

        public FamilyGroup apply() {
            FamilyGroup.this.validate();
            return FamilyGroup.this;
        }
    }

    public UUID getId() {
        return id;
    }

    public String getFriendlyId() {
        return friendlyId;
    }

    public String getName() {
        return name;
    }

    public BigDecimal getHouseholdIncome() {
        return householdIncome;
    }

    public BigDecimal getPerCapitaIncome() {
        return perCapitaIncome;
    }

    public BigDecimal getEducationExpense() {
        return educationExpense;
    }

    public BigDecimal getHealthExpense() {
        return healthExpense;
    }

    public BigDecimal getHousingExpense() {
        return housingExpense;
    }

    public Integer getNumberOfResidents() {
        return numberOfResidents;
    }

    public Address getAddress() {
        return address;
    }

    public List<NaturalPerson> getNaturalPersonList() {
        return naturalPersonList;
    }
}
