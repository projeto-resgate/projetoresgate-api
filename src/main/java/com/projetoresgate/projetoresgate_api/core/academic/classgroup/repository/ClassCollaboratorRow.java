package com.projetoresgate.projetoresgate_api.core.academic.classgroup.repository;

import java.time.LocalDateTime;
import java.util.UUID;

public interface ClassCollaboratorRow {

    UUID getId();

    UUID getClassGroupId();

    UUID getCollaboratorId();

    String getCollaboratorName();

    UUID getEducatorCategoryItemId();

    String getEducatorCategoryItemName();

    boolean getActive();

    LocalDateTime getDateCreated();
}
