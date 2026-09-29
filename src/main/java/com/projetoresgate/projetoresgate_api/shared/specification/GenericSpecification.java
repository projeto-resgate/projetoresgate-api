package com.projetoresgate.projetoresgate_api.shared.specification;

import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Expression;
import jakarta.persistence.criteria.Path;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.lang.NonNull;

import java.util.List;
import java.util.Locale;

public class GenericSpecification<T> implements Specification<T> {

    public static final String EQUALS = ":";
    public static final String ILIKE = "~";
    public static final String GREATER_THAN = ">";
    public static final String GREATER_OR_EQUAL = ">=";
    public static final String LESS_THAN = "<";
    public static final String LESS_OR_EQUAL = "<=";

    private static final List<String> SUPPORTED_OPERATIONS =
            List.of(EQUALS, ILIKE, GREATER_THAN, GREATER_OR_EQUAL, LESS_THAN, LESS_OR_EQUAL);

    private static final char ESCAPE_CHAR = '\\';

    private final SearchCriteria criteria;

    public GenericSpecification(SearchCriteria criteria) {
        this.criteria = criteria;
    }

    @Override
    public Predicate toPredicate(@NonNull Root<T> root, CriteriaQuery<?> query, @NonNull CriteriaBuilder builder) {
        Path<?> path = getPath(root, criteria.key());
        Object value = criteria.value();

        return switch (criteria.operation()) {
            case EQUALS -> builder.equal(path, value);
            case ILIKE -> ilike(path, value, builder);
            case GREATER_THAN -> greaterThan(path, value, builder);
            case GREATER_OR_EQUAL -> greaterOrEqual(path, value, builder);
            case LESS_THAN -> lessThan(path, value, builder);
            case LESS_OR_EQUAL -> lessOrEqual(path, value, builder);
            default -> throw new IllegalArgumentException(unknownOperation());
        };
    }

    static boolean isSupported(String operation) {
        return SUPPORTED_OPERATIONS.contains(operation);
    }

    private Predicate ilike(Path<?> path, Object value, CriteriaBuilder builder) {
        if (!isString(path)) {
            throw new IllegalArgumentException(
                    "A operação \"~\" (ILIKE) só funciona em propriedade de texto, mas \"%s\" não é texto. Use \":\" para igualdade."
                            .formatted(criteria.key()));
        }

        String pattern = "%" + escape(value.toString().toLowerCase(Locale.ROOT)) + "%";
        return builder.like(builder.lower(asString(path)), pattern, ESCAPE_CHAR);
    }

    /**
     * ILIKE não faz sentido em coluna numérica, de data ou de enum: o usuário não busca "parte"
     * de um número, e o Postgres nem tem o operador para isso. Sem esta checagem, o cast para
     * Expression<String> falharia só na hora de rodar a query, com mensagem de erro do banco.
     */
    private boolean isString(Path<?> path) {
        return path.getJavaType() == String.class;
    }

    @SuppressWarnings("unchecked")
    private Expression<String> asString(Path<?> path) {
        return (Expression<String>) path;
    }

    private String escape(String value) {
        return value.replace("\\", "\\\\")
                .replace("%", "\\%")
                .replace("_", "\\_");
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    private Predicate greaterThan(Path<?> path, Object value, CriteriaBuilder builder) {
        return builder.greaterThan((Expression) path, (Comparable) value);
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    private Predicate greaterOrEqual(Path<?> path, Object value, CriteriaBuilder builder) {
        return builder.greaterThanOrEqualTo((Expression) path, (Comparable) value);
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    private Predicate lessThan(Path<?> path, Object value, CriteriaBuilder builder) {
        return builder.lessThan((Expression) path, (Comparable) value);
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    private Predicate lessOrEqual(Path<?> path, Object value, CriteriaBuilder builder) {
        return builder.lessThanOrEqualTo((Expression) path, (Comparable) value);
    }

    private String unknownOperation() {
        return "Operação não suportada: \"%s\". Use uma de: %s"
                .formatted(criteria.operation(), String.join(", ", SUPPORTED_OPERATIONS));
    }

    private Path<?> getPath(Root<T> root, String key) {
        if (!key.contains(".")) {
            return root.get(key);
        }
        String[] keys = key.split("\\.");
        Path<?> path = root.get(keys[0]);
        for (int i = 1; i < keys.length; i++) {
            path = path.get(keys[i]);
        }
        return path;
    }
}
