package com.projetoresgate.projetoresgate_api.core.physicalperson.service;

import com.projetoresgate.projetoresgate_api.core.physicalperson.domain.PhysicalPerson;
import com.projetoresgate.projetoresgate_api.core.physicalperson.repository.PhysicalPersonRepository;
import com.projetoresgate.projetoresgate_api.core.physicalperson.usecase.SearchPhysicalPersonUseCase;
import com.projetoresgate.projetoresgate_api.core.physicalperson.usecase.query.SearchPhysicalPersonQuery;
import com.projetoresgate.projetoresgate_api.core.user.domain.User;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.domain.Page;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;

@Service
public class SearchPhysicalPersonService implements SearchPhysicalPersonUseCase {

    private final PhysicalPersonRepository repository;

    public SearchPhysicalPersonService(PhysicalPersonRepository repository) {
        this.repository = repository;
    }

    @Override
    public Page<PhysicalPerson> handle(SearchPhysicalPersonQuery query) {
        Specification<PhysicalPerson> spec = (root, criteriaQuery, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (StringUtils.hasText(query.searchTerm())) {
                String raw = query.searchTerm();
                String textSearch = "%" + raw.toLowerCase() + "%";
                Join<PhysicalPerson, User> userJoin = root.join("user");

                List<Predicate> orPredicates = new ArrayList<>();
                orPredicates.add(criteriaBuilder.like(criteriaBuilder.lower(userJoin.get("name")), textSearch));
                orPredicates.add(criteriaBuilder.like(criteriaBuilder.lower(userJoin.get("nickname")), textSearch));

                String digits = raw.replaceAll("\\D", "");
                if (StringUtils.hasText(digits)) {
                    orPredicates.add(criteriaBuilder.like(root.get("cpf").get("value"), "%" + digits + "%"));
                }

                String alphanumeric = raw.replaceAll("[^a-zA-Z0-9]", "");
                if (StringUtils.hasText(alphanumeric)) {
                    orPredicates.add(criteriaBuilder.like(criteriaBuilder.upper(root.get("rg").get("value")), "%" + alphanumeric.toUpperCase() + "%"));
                }

                predicates.add(criteriaBuilder.or(orPredicates.toArray(new Predicate[0])));
            }

            if (query.cpf() != null && query.cpf().getValue() != null) {
                predicates.add(criteriaBuilder.like(root.get("cpf").get("value"), "%" + query.cpf().getValue() + "%"));
            }

            if (query.rg() != null && query.rg().getValue() != null) {
                predicates.add(criteriaBuilder.like(root.get("rg").get("value"), "%" + query.rg().getValue() + "%"));
            }

            if (StringUtils.hasText(query.cellphone())) {
                predicates.add(criteriaBuilder.like(root.get("cellphone"), "%" + query.cellphone() + "%"));
            }

            if (query.gender() != null) {
                predicates.add(criteriaBuilder.equal(root.get("gender"), query.gender()));
            }

            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        };

        return repository.findAll(spec, query.pageable());
    }
}
