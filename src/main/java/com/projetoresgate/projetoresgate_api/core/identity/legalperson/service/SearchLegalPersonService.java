package com.projetoresgate.projetoresgate_api.core.identity.legalperson.service;

import com.projetoresgate.projetoresgate_api.core.identity.legalperson.domain.LegalPerson;
import com.projetoresgate.projetoresgate_api.core.identity.legalperson.repository.LegalPersonRepository;
import com.projetoresgate.projetoresgate_api.core.identity.legalperson.usecase.SearchLegalPersonUseCase;
import com.projetoresgate.projetoresgate_api.core.identity.legalperson.usecase.query.SearchLegalPersonQuery;
import com.projetoresgate.projetoresgate_api.shared.specification.SpecificationBuilder;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.domain.Page;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;

@Service
public class SearchLegalPersonService implements SearchLegalPersonUseCase {

    private final LegalPersonRepository repository;

    public SearchLegalPersonService(LegalPersonRepository repository) {
        this.repository = repository;
    }

    @Override
    public Page<LegalPerson> handle(SearchLegalPersonQuery query) {

        Specification<LegalPerson> genericFilters = new SpecificationBuilder<LegalPerson>()
                .with("cnpj", ":", query.cnpj())
                .with("corporateName", ":", query.corporateName())
                .with("registrationStatus", ":", query.registrationStatus())
                .with("companyStatus", ":", query.companyStatus())
                .build();

        Specification<LegalPerson> finalSpec = Specification
                .where(hasSearchTerm(query.searchTerm()))
                .and(genericFilters);

        return repository.findAll(finalSpec, query.pageable());
    }

    private Specification<LegalPerson> hasSearchTerm(String searchTerm) {
        if (!StringUtils.hasText(searchTerm)) return null;

        return (root, criteriaQuery, cb) -> {
            String textSearch = "%" + searchTerm.toLowerCase() + "%";

            List<Predicate> orPredicates = new ArrayList<>();
            orPredicates.add(cb.like(cb.lower(root.get("corporateName")), textSearch));
            orPredicates.add(cb.like(cb.lower(root.get("tradeName")), textSearch));
            orPredicates.add(cb.like(cb.lower(root.get("displayName")), textSearch));

            String digits = searchTerm.replaceAll("\\D", "");
            if (StringUtils.hasText(digits)) {
                orPredicates.add(cb.like(root.get("cnpj"), "%" + digits + "%"));
            }

            return cb.or(orPredicates.toArray(new Predicate[0]));
        };
    }
}
