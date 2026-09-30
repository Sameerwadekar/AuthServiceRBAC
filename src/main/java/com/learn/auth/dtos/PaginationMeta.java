package com.learn.auth.dtos;

import org.springframework.data.domain.Page;

public record PaginationMeta(
        int page,
        int size,
        long totalElements,
        int totalPages,
        boolean isFirst,
        boolean isLast
) {
    // Factory method to map a Spring Page metadata instantly
    public static PaginationMeta from(Page<?> page) {
        return new PaginationMeta(
                page.getNumber(),
                page.getSize(),
                page.getTotalElements(),
                page.getTotalPages(),
                page.isFirst(),
                page.isLast()
        );
    }
}
