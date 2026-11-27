package com.projetoresgate.projetoresgate_api.core.academic.classgroup.repository;

import java.util.UUID;

public interface LearnerClassGroupRow {

    UUID getLearnerId();

    UUID getClassGroupId();

    String getTitle();
}
