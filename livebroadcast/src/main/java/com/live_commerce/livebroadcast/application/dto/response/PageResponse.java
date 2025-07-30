package com.live_commerce.livebroadcast.application.dto.response;

import org.springframework.data.domain.Page;

import java.util.List;

public record PageResponse<T>(
        List<T> contents,
        PaginationMeta pagination
) {
    public static <T> PageResponse<T> from(Page<T> page) {
        return new PageResponse<>(
                page.getContent(),
                new PaginationMeta(
                        page.getNumber(),
                        page.getSize(),
                        page.getTotalPages(),
                        page.getTotalElements(),
                        page.isLast()
                )
        );
    }

    public record PaginationMeta(
            int currentPage,
            int pageSize,
            int totalPages,
            long totalElements,
            boolean isLast
    ) {}
}
