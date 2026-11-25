package com.projetoresgate.projetoresgate_api.core.identity.naturalperson.service;

import com.projetoresgate.projetoresgate_api.core.identity.naturalperson.domain.NaturalPerson;
import com.projetoresgate.projetoresgate_api.core.identity.naturalperson.repository.NaturalPersonRepository;
import com.projetoresgate.projetoresgate_api.core.identity.naturalperson.usecase.FindAllNaturalPersonsUseCase;
import com.projetoresgate.projetoresgate_api.core.identity.naturalperson.usecase.query.FindAllNaturalPersonsQuery;
import com.projetoresgate.projetoresgate_api.shared.specification.SpecificationBuilder;
import com.projetoresgate.projetoresgate_api.shared.specification.Specs;
import com.projetoresgate.projetoresgate_api.shared.util.MaskUtils;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.domain.Page;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;

@Service
public class FindAllNaturalPersonsService implements FindAllNaturalPersonsUseCase {

    private final NaturalPersonRepository repository;

    public FindAllNaturalPersonsService(NaturalPersonRepository repository) {
        this.repository = repository;
    }

    @Override
    public Page<NaturalPerson> handle(FindAllNaturalPersonsQuery query) {

        Specification<NaturalPerson> genericFilters = new SpecificationBuilder<NaturalPerson>()
                .with("cpf", "~", onlyDigits(query.cpf()))
                .with("rg", "~", MaskUtils.unmaskUpperCase(query.rg()))
                .with("cellphone", "~", onlyDigits(query.cellphone()))
                .with("gender", ":", query.gender())
                .build();

        Specification<NaturalPerson> finalSpec = Specs.allOf(hasSearchTerm(query.searchTerm()), genericFilters);

        return repository.findAll(finalSpec, query.pageable());
    }

    private Specification<NaturalPerson> hasSearchTerm(String searchTerm) {
        if (!StringUtils.hasText(searchTerm)) return null;

        return (root, criteriaQuery, cb) -> {
            String textSearch = "%" + searchTerm.toLowerCase() + "%";

            List<Predicate> orPredicates = new ArrayList<>();
            orPredicates.add(cb.like(cb.lower(root.get("name")), textSearch));
            orPredicates.add(cb.like(cb.lower(root.get("nickname")), textSearch));

            String digits = searchTerm.replaceAll("\\D", "");
            if (StringUtils.hasText(digits)) {
                orPredicates.add(cb.like(root.get("cpf"), "%" + digits + "%"));
            }

            String alphanumeric = searchTerm.replaceAll("[^a-zA-Z0-9]", "");
            if (StringUtils.hasText(alphanumeric)) {
                orPredicates.add(cb.like(
                            cb.upper(cb.function("regexp_replace", String.class, root.get("rg"),
                                    cb.literal("[^0-9A-Za-z]"), cb.literal(""), cb.literal("g"))),
                            "%" + alphanumeric.toUpperCase() + "%"));
            }

            return cb.or(orPredicates.toArray(new Predicate[0]));
        };
    }

    private static String onlyDigits(String value) {
        if (!StringUtils.hasText(value)) {
            return null;
        }
        String digits = value.replaceAll("\\D", "");
        return digits.isEmpty() ? value : digits;
    }
}
