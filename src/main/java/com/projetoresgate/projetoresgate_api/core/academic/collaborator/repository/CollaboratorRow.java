package com.projetoresgate.projetoresgate_api.core.academic.collaborator.repository;

import java.util.UUID;

public interface CollaboratorRow {

    UUID getId();

    UUID getNaturalPersonId();

    String getNaturalPersonName();

    String getNaturalPersonCpf();

    UUID getLegalPersonId();

    String getLegalPersonName();

    boolean getIsVoluntary();
}
