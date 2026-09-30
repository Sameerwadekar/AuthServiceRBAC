package com.learn.auth.dtos;

import jakarta.persistence.criteria.Path;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;

import java.util.ArrayList;
import java.util.List;

public class GenericSpecification {

    /**
     * Builds a specification for tenant-isolated entities with optional search keywords.
     *
     * @param tenantId      The UUID string of the tenant (optional/nullable)
     * @param searchKeyword Text to search across specified fields
     * @param searchFields  Entity field names to apply LIKE matching on
     */
    public static <T> Specification<T> searchAndFilter(String tenantId, String searchKeyword, List<String> searchFields) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            // 1. Multi-tenant isolation (only applied if tenantId is provided)
            if (tenantId != null && !tenantId.isBlank()) {
                try {
                    Path<?> tenantPath;
                    try {
                        tenantPath = root.get("tenant").get("id");
                    } catch (Exception e) {
                        tenantPath = root.get("tenantId");
                    }
                    predicates.add(cb.equal(tenantPath, tenantId.trim()));
                } catch (Exception ignored) {
                    // Entity does not have tenant relation
                }
            }

            // 2. Keyword search across specified fields (OR condition)
            if (searchKeyword != null && !searchKeyword.isBlank() && searchFields != null && !searchFields.isEmpty()) {
                String matchExpression = "%" + searchKeyword.trim().toLowerCase() + "%";
                List<Predicate> searchPredicates = new ArrayList<>();

                for (String field : searchFields) {
                    try {
                        searchPredicates.add(
                                cb.like(cb.lower(root.get(field).as(String.class)), matchExpression)
                        );
                    } catch (Exception ignored) {
                    }
                }

                if (!searchPredicates.isEmpty()) {
                    predicates.add(cb.or(searchPredicates.toArray(new Predicate[0])));
                }
            }

            return predicates.isEmpty() ? cb.conjunction() : cb.and(predicates.toArray(new Predicate[0]));
        };
    }

    public static <T> Specification<T> search(String searchKeyword, List<String> searchFields) {
        return searchAndFilter(null, searchKeyword, searchFields);
    }

    public static <T> Specification<T> search(String tenantId, String searchKeyword, List<String> searchFields) {
        return searchAndFilter(tenantId, searchKeyword, searchFields);
    }
}
