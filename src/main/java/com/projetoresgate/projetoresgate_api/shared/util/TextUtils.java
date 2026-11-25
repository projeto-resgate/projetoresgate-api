package com.projetoresgate.projetoresgate_api.shared.util;

import org.springframework.util.StringUtils;

public final class TextUtils {

    private TextUtils() {
    }

    public static String blankToNull(String value) {
        return StringUtils.hasText(value) ? value : null;
    }
}
