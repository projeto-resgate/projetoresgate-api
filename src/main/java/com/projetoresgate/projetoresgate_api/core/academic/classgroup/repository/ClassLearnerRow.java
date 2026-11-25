package com.projetoresgate.projetoresgate_api.core.academic.classgroup.repository;

import java.time.LocalDateTime;
import java.util.UUID;

public interface ClassLearnerRow {

    UUID getId();

    UUID getClassGroupId();

    UUID getLearnerId();

    String getLearnerName();

    String getLearnerCpf();

    String getLearnerStatus();

    boolean getActive();

    LocalDateTime getDateCreated();
}
