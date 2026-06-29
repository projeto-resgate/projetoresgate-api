package com.projetoresgate.projetoresgate_api.core.identity.naturalperson.service;

import com.projetoresgate.projetoresgate_api.core.identity.naturalperson.domain.NaturalPerson;
import com.projetoresgate.projetoresgate_api.core.identity.naturalperson.repository.NaturalPersonRepository;
import com.projetoresgate.projetoresgate_api.core.identity.naturalperson.usecase.SearchNaturalPersonUseCase;
import com.projetoresgate.projetoresgate_api.core.identity.naturalperson.usecase.query.SearchNaturalPersonQuery;
import com.projetoresgate.projetoresgate_api.core.identity.user.domain.User;
import com.projetoresgate.projetoresgate_api.shared.specification.SpecificationBuilder;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.domain.Page;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;

@Service
public class SearchNaturalPersonService implements SearchNaturalPersonUseCase {

    private final NaturalPersonRepository repository;

    public SearchNaturalPersonService(NaturalPersonRepository repository) {
        this.repository = repository;
    }

    @Override
    public Page<NaturalPerson> handle(SearchNaturalPersonQuery query) {

        Specification<NaturalPerson> genericFilters = new SpecificationBuilder<NaturalPerson>()
                .with("cpf", ":", query.cpf())
                .with("rg", ":", query.rg())
                .with("cellphone", ":", query.cellphone())
                .with("gender", ":", query.gender())
                .build();

        Specification<NaturalPerson> finalSpec = Specification
                .where(hasSearchTerm(query.searchTerm()))
                .and(genericFilters);

        return repository.findAll(finalSpec, query.pageable());
    }

    private Specification<NaturalPerson> hasSearchTerm(String searchTerm) {
        if (!StringUtils.hasText(searchTerm)) return null;

        return (root, criteriaQuery, cb) -> {
            String textSearch = "%" + searchTerm.toLowerCase() + "%";
            Join<NaturalPerson, User> userJoin = root.join("user");

            List<Predicate> orPredicates = new ArrayList<>();
            orPredicates.add(cb.like(cb.lower(userJoin.get("name")), textSearch));
            orPredicates.add(cb.like(cb.lower(userJoin.get("nickname")), textSearch));

            String digits = searchTerm.replaceAll("\\D", "");
            if (StringUtils.hasText(digits)) {
                orPredicates.add(cb.like(root.get("cpf"), "%" + digits + "%"));
            }

            String alphanumeric = searchTerm.replaceAll("[^a-zA-Z0-9]", "");
            if (StringUtils.hasText(alphanumeric)) {
                orPredicates.add(cb.like(cb.upper(root.get("rg")), "%" + alphanumeric.toUpperCase() + "%"));
            }

            return cb.or(orPredicates.toArray(new Predicate[0]));
        };
    }
}