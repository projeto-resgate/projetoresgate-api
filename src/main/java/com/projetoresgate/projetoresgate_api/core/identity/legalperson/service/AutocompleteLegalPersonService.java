package com.projetoresgate.projetoresgate_api.core.identity.legalperson.service;

import com.projetoresgate.projetoresgate_api.core.identity.legalperson.api.dto.LegalPersonSummaryResponse;
import com.projetoresgate.projetoresgate_api.core.identity.legalperson.domain.LegalPerson;
import com.projetoresgate.projetoresgate_api.core.identity.legalperson.repository.LegalPersonRepository;
import com.projetoresgate.projetoresgate_api.core.identity.legalperson.usecase.AutocompleteLegalPersonUseCase;
import com.projetoresgate.projetoresgate_api.core.identity.legalperson.usecase.query.AutocompleteLegalPersonQuery;
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
public class AutocompleteLegalPersonService implements AutocompleteLegalPersonUseCase {

    private final LegalPersonRepository repository;

    public AutocompleteLegalPersonService(LegalPersonRepository repository) {
        this.repository = repository;
    }

    @Override
    public List<LegalPersonSummaryResponse> handle(AutocompleteLegalPersonQuery query) {
        Specification<LegalPerson> spec = (root, criteriaQuery, cb) -> {
            List<Predicate> orPredicates = new ArrayList<>();

            if (StringUtils.hasText(query.searchTerm())) {
                String textSearch = "%" + query.searchTerm().toLowerCase() + "%";
                orPredicates.add(cb.like(cb.lower(root.get("corporateName")), textSearch));
                orPredicates.add(cb.like(cb.lower(root.get("tradeName")), textSearch));
                orPredicates.add(cb.like(cb.lower(root.get("displayName")), textSearch));

                String digits = query.searchTerm().replaceAll("\\D", "");
                if (StringUtils.hasText(digits)) {
                    orPredicates.add(cb.like(root.get("cnpj"), "%" + digits + "%"));
                }
            }

            if (orPredicates.isEmpty()) {
                return cb.conjunction();
            }

            return cb.or(orPredicates.toArray(new Predicate[0]));
        };

        Pageable pageable = PageRequest.of(0, query.limit(), Sort.by("displayName").ascending());
        return repository.findAll(spec, pageable).getContent().stream()
                .map(LegalPersonSummaryResponse::fromEntity)
                .toList();
    }
}