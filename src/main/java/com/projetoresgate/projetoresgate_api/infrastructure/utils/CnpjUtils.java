package com.projetoresgate.projetoresgate_api.infrastructure.utils;

public final class CnpjUtils {

    private CnpjUtils() {
    }

    public static String onlyDigits(String cnpj) {
        if (cnpj == null) return null;
        return cnpj.replaceAll("\\D", "");
    }
}