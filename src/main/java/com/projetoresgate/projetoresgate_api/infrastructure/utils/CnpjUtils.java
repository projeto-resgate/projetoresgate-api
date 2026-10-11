package com.projetoresgate.projetoresgate_api.infrastructure.utils;

import static java.util.Objects.isNull;

public final class CnpjUtils {

    private CnpjUtils() {
    }

    public static String onlyDigits(String cnpj) {
        if (isNull(cnpj)) {
            return null;
        }
        return cnpj.replaceAll("\\D", "");
    }
}
