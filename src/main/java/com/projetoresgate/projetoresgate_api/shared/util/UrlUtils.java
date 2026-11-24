package com.projetoresgate.projetoresgate_api.shared.util;

public final class UrlUtils {

    private UrlUtils() {
    }

    public static String removeTrailingSlashes(String url) {
        int end = url.length();
        while (end > 0 && url.charAt(end - 1) == '/') {
            end--;
        }
        return url.substring(0, end);
    }
}
