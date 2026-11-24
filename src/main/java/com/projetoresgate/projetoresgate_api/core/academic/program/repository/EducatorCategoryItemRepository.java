package com.projetoresgate.projetoresgate_api.core.academic.program.repository;

import java.util.List;

import java.util.Collection;

import org.springframework.data.repository.query.Param;

import org.springframework.data.jpa.repository.Query;

import com.projetoresgate.projetoresgate_api.core.academic.program.domain.EducatorCategoryItem;
import com.projetoresgate.projetoresgate_api.infrastructure.exception.ResourceNotFoundException;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface EducatorCategoryItemRepository extends JpaRepository<EducatorCategoryItem, UUID>,
        JpaSpecificationExecutor<EducatorCategoryItem> {

    Optional<EducatorCategoryItem> findByIdAndProgramId(UUID id, UUID programId);

    default EducatorCategoryItem findByIdAndProgramIdOrThrow(UUID id, UUID programId) {
        return findByIdAndProgramId(id, programId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Categoria de educador não encontrada com ID: " + id + " no programa: " + programId));
    }

    @Query(value = "SELECT * FROM educator_category_item WHERE id IN (:ids)", nativeQuery = true)
    List<EducatorCategoryItem> findAllByIdIncludingDeleted(@Param("ids") Collection<UUID> ids);

    default Optional<EducatorCategoryItem> findByIdIncludingDeleted(UUID id) {
        return findAllByIdIncludingDeleted(List.of(id)).stream().findFirst();
    }
}
