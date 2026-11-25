package com.projetoresgate.projetoresgate_api.core.academic.classgroup.repository;

import java.util.UUID;

public interface ClassGroupRow {

    UUID getId();

    String getTitle();

    UUID getProgramId();

    String getProgramName();

    UUID getInstitutionId();

    String getInstitutionName();

    boolean getActive();
}
