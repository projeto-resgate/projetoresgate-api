package com.projetoresgate.projetoresgate_api.shared.deletion;

import java.util.Arrays;
import java.util.List;

public final class DeletionGuard {

    private DeletionGuard() {
    }

    public record Dependency(long count, String singular, String plural) {

        public static Dependency of(long count, String singular, String plural) {
            return new Dependency(count, singular, plural);
        }

        private String describe() {
            return count + " " + (count == 1 ? singular : plural);
        }
    }

    public static void ensureNoDependencies(String entityLabel, Dependency... dependencies) {
        List<String> found = Arrays.stream(dependencies)
                .filter(dependency -> dependency.count() > 0)
                .map(Dependency::describe)
                .toList();

        if (found.isEmpty()) {
            return;
        }

        throw new IllegalStateException("Não é possível excluir " + entityLabel
                + " porque existem vínculos com: " + joinWithAnd(found)
                + ". Remova os vínculos antes de excluir.");
    }

    private static String joinWithAnd(List<String> items) {
        if (items.size() == 1) {
            return items.getFirst();
        }
        return String.join(", ", items.subList(0, items.size() - 1)) + " e " + items.getLast();
    }
}
