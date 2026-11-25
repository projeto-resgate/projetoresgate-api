package com.projetoresgate.projetoresgate_api.core.academic.learner.repository;

import java.util.UUID;

public interface LearnerAutocompleteRow {

    UUID getId();

    String getName();

    String getCpf();

    String getStatus();
}
