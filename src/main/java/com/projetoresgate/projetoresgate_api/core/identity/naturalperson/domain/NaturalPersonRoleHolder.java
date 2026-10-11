package com.projetoresgate.projetoresgate_api.core.identity.naturalperson.domain;

import com.projetoresgate.projetoresgate_api.core.identity.naturalperson.domain.enums.NaturalPersonRoleType;

import java.util.UUID;

public interface NaturalPersonRoleHolder {

    UUID naturalPersonId();

    NaturalPersonRoleType role();
}
