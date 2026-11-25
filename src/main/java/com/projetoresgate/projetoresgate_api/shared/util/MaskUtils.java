package com.projetoresgate.projetoresgate_api.shared.util;

import org.springframework.util.StringUtils;

import java.util.Locale;

public final class MaskUtils {

    private MaskUtils() {
    }

    public static String unmask(String value) {
        if (!StringUtils.hasText(value)) {
            return null;
        }
        String clean = value.replaceAll("[\\s.\\-/()]", "");
        return clean.isEmpty() ? null : clean;
    }

    public static String unmaskUpperCase(String value) {
        String clean = unmask(value);
        return StringUtils.hasText(clean) ? clean.toUpperCase(Locale.ROOT) : clean;
    }
}
