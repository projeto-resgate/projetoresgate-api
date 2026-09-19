package com.projetoresgate.projetoresgate_api.core.identity.naturalperson.service;

import com.projetoresgate.projetoresgate_api.core.identity.naturalperson.api.dto.NaturalPersonSummaryResponse;
import com.projetoresgate.projetoresgate_api.core.identity.naturalperson.domain.NaturalPerson;
import com.projetoresgate.projetoresgate_api.core.identity.naturalperson.repository.NaturalPersonRepository;
import com.projetoresgate.projetoresgate_api.core.identity.naturalperson.usecase.AutocompleteNaturalPersonUseCase;
import com.projetoresgate.projetoresgate_api.core.identity.naturalperson.usecase.query.AutocompleteNaturalPersonQuery;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;

@Service
public class AutocompleteNaturalPersonService implements AutocompleteNaturalPersonUseCase {

    private final NaturalPersonRepository repository;

    public AutocompleteNaturalPersonService(NaturalPersonRepository repository) {
        this.repository = repository;
    }

    @Override
    public List<NaturalPersonSummaryResponse> handle(AutocompleteNaturalPersonQuery query) {
        Specification<NaturalPerson> spec = (root, criteriaQuery, cb) -> {
            List<Predicate> orPredicates = new ArrayList<>();

            if (StringUtils.hasText(query.searchTerm())) {
                String textSearch = "%" + query.searchTerm().toLowerCase() + "%";
                orPredicates.add(cb.like(cb.lower(root.get("name")), textSearch));
                orPredicates.add(cb.like(cb.lower(root.get("nickname")), textSearch));

                String digits = query.searchTerm().replaceAll("\\D", "");
                if (StringUtils.hasText(digits)) {
                    orPredicates.add(cb.like(root.get("cpf"), "%" + digits + "%"));
                }

                String alphanumeric = query.searchTerm().replaceAll("[^a-zA-Z0-9]", "");
                if (StringUtils.hasText(alphanumeric)) {
                    orPredicates.add(cb.like(cb.upper(root.get("rg")), "%" + alphanumeric.toUpperCase() + "%"));
                }
            }

            if (orPredicates.isEmpty()) {
                return cb.conjunction();
            }

            return cb.or(orPredicates.toArray(new Predicate[0]));
        };

        Pageable pageable = PageRequest.of(0, query.limit(), Sort.by("name").ascending());
        return repository.findAll(spec, pageable).getContent().stream()
                .map(NaturalPersonSummaryResponse::fromEntity)
                .toList();
    }
}