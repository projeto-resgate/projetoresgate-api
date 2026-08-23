package com.projetoresgate.projetoresgate_api.core.identity.legalperson.domain;

import com.projetoresgate.projetoresgate_api.core.identity.legalperson.domain.enums.CompanyStatus;
import com.projetoresgate.projetoresgate_api.core.identity.legalperson.domain.enums.RegistrationStatus;
import com.projetoresgate.projetoresgate_api.infrastructure.exception.InternalException;
import com.projetoresgate.projetoresgate_api.shared.domain.Address;
import com.projetoresgate.projetoresgate_api.shared.entity.AuditableEntity;
import jakarta.persistence.*;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;
import org.springframework.util.StringUtils;

import java.util.UUID;

import static java.util.Objects.nonNull;

@Entity
@Table(name = "legal_person")
@SQLDelete(sql = "UPDATE legal_person SET deleted_at = now() WHERE id = ?")
@SQLRestriction("deleted_at IS NULL")
public class LegalPerson extends AuditableEntity {

    @Id
    private UUID id;

    private String cnpj;

    private String corporateName;

    private String tradeName;

    private String displayName;

    private String mainCnaeCode;

    @Enumerated(EnumType.STRING)
    private RegistrationStatus registrationStatus;

    @Enumerated(EnumType.STRING)
    private CompanyStatus companyStatus;

    @Embedded
    private Address address;

    @Embedded
    private Representative representative;

    protected LegalPerson() {
    }

    private LegalPerson(UUID id, String cnpj, String corporateName, String tradeName, String displayName,
                        String mainCnaeCode, RegistrationStatus registrationStatus, CompanyStatus companyStatus,
                        Address address, Representative representative) {
        this.id = id;
        this.cnpj = cnpj;
        this.corporateName = corporateName;
        this.tradeName = tradeName;
        this.displayName = displayName;
        this.mainCnaeCode = mainCnaeCode;
        this.registrationStatus = registrationStatus;
        this.companyStatus = companyStatus;
        this.address = address;
        this.representative = representative;
        validate();
    }

    public static LegalPerson create(String cnpj, String corporateName, String tradeName, String displayName,
                                     String mainCnaeCode, RegistrationStatus registrationStatus,
                                     CompanyStatus companyStatus, Address address) {
        return new LegalPerson(UUID.randomUUID(), cnpj, corporateName, tradeName, displayName,
                mainCnaeCode, registrationStatus, companyStatus, address, null);
    }

    public static LegalPerson create(String cnpj, String corporateName, String tradeName, String displayName,
                                     String mainCnaeCode, RegistrationStatus registrationStatus,
                                     CompanyStatus companyStatus, Address address, Representative representative) {
        return new LegalPerson(UUID.randomUUID(), cnpj, corporateName, tradeName, displayName,
                mainCnaeCode, registrationStatus, companyStatus, address, representative);
    }

    public void validate() {
        if (!StringUtils.hasText(this.cnpj)) {
            throw new InternalException("O CNPJ não pode ser vazio.");
        }
        if (this.cnpj.length() > 14) {
            throw new InternalException("O CNPJ não pode exceder 14 caracteres.");
        }
        if (!StringUtils.hasText(this.corporateName)) {
            throw new InternalException("A razão social não pode ser vazia.");
        }
        if (StringUtils.hasText(this.mainCnaeCode) && this.mainCnaeCode.length() > 20) {
            throw new InternalException("O CNAE principal não pode exceder 20 caracteres.");
        }
        if (!nonNull(this.registrationStatus)) {
            throw new InternalException("O status de registro é obrigatório.");
        }
        if (!nonNull(this.companyStatus)) {
            throw new InternalException("O status da empresa é obrigatório.");
        }
        if (!nonNull(this.address)) {
            throw new InternalException("O endereço é obrigatório.");
        }
        this.address.validate();
        if (nonNull(this.representative)) {
            this.representative.validate();
        }
    }

    public Updater update() {
        return new Updater();
    }

    public class Updater {

        public Updater cnpj(String cnpj) {
            LegalPerson.this.cnpj = cnpj;
            return this;
        }

        public Updater corporateName(String corporateName) {
            LegalPerson.this.corporateName = corporateName;
            return this;
        }

        public Updater tradeName(String tradeName) {
            LegalPerson.this.tradeName = tradeName;
            return this;
        }

        public Updater displayName(String displayName) {
            LegalPerson.this.displayName = displayName;
            return this;
        }

        public Updater mainCnaeCode(String mainCnaeCode) {
            LegalPerson.this.mainCnaeCode = mainCnaeCode;
            return this;
        }

        public Updater registrationStatus(RegistrationStatus registrationStatus) {
            LegalPerson.this.registrationStatus = registrationStatus;
            return this;
        }

        public Updater companyStatus(CompanyStatus companyStatus) {
            LegalPerson.this.companyStatus = companyStatus;
            return this;
        }

        public Updater address(Address address) {
            LegalPerson.this.address = address;
            return this;
        }

        public Updater representative(Representative representative) {
            LegalPerson.this.representative = representative;
            return this;
        }

        public LegalPerson apply() {
            LegalPerson.this.validate();
            return LegalPerson.this;
        }
    }

    public UUID getId() {
        return id;
    }

    public String getCnpj() {
        return cnpj;
    }

    public String getCorporateName() {
        return corporateName;
    }

    public String getTradeName() {
        return tradeName;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getMainCnaeCode() {
        return mainCnaeCode;
    }

    public RegistrationStatus getRegistrationStatus() {
        return registrationStatus;
    }

    public CompanyStatus getCompanyStatus() {
        return companyStatus;
    }

    public Address getAddress() {
        return address;
    }

    public Representative getRepresentative() {
        return representative;
    }
}
