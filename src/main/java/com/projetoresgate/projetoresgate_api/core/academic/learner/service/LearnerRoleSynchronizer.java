package com.projetoresgate.projetoresgate_api.core.academic.learner.service;

import com.projetoresgate.projetoresgate_api.core.academic.learner.domain.Learner;
import com.projetoresgate.projetoresgate_api.core.academic.learner.domain.Responsible;
import com.projetoresgate.projetoresgate_api.core.academic.learner.repository.LearnerRepository;
import com.projetoresgate.projetoresgate_api.core.academic.learner.repository.ResponsibleRepository;
import com.projetoresgate.projetoresgate_api.core.identity.naturalperson.domain.NaturalPersonRole;
import com.projetoresgate.projetoresgate_api.core.identity.naturalperson.domain.NaturalPersonRoleHolder;
import com.projetoresgate.projetoresgate_api.core.identity.naturalperson.domain.enums.NaturalPersonRoleType;
import com.projetoresgate.projetoresgate_api.core.identity.naturalperson.repository.NaturalPersonRoleRepository;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Component
public class LearnerRoleSynchronizer {

    private final NaturalPersonRoleRepository roleRepository;
    private final ResponsibleRepository responsibleRepository;
    private final LearnerRepository learnerRepository;

    public LearnerRoleSynchronizer(NaturalPersonRoleRepository roleRepository,
                                   ResponsibleRepository responsibleRepository,
                                   LearnerRepository learnerRepository) {
        this.roleRepository = roleRepository;
        this.responsibleRepository = responsibleRepository;
        this.learnerRepository = learnerRepository;
    }

    public void declare(Learner learner) {
        for (NaturalPersonRoleHolder holder : learner.roleAssignments()) {
            if (roleRepository.findByNaturalPersonIdAndRole(holder.naturalPersonId(), holder.role()).isPresent()) {
                continue;
            }

            roleRepository.save(NaturalPersonRole.create(holder.naturalPersonId(), holder.role()));
        }
    }

    public void forgetRemoved(List<Responsible> removed) {
        learnerRepository.flush();

        revokeResponsibles(removed);
    }

    public void revokeEverything(Learner learner) {
        learnerRepository.flush();

        revokeBy(learner.getNaturalPersonId(), learner.role());
        revokeResponsibles(learner.getResponsibleList());
    }

    private void revokeResponsibles(List<Responsible> responsibles) {
        for (Responsible responsible : responsibles) {
            if (responsibleRepository.countActiveByNaturalPersonId(responsible.getNaturalPersonId()) == 0) {
                revokeBy(responsible.getNaturalPersonId(), responsible.role());
            }
        }
    }

    private void revokeBy(UUID naturalPersonId, NaturalPersonRoleType role) {
        roleRepository.revokeByNaturalPersonIdAndRole(naturalPersonId, role, LocalDateTime.now());
    }
}