package com.live_commerce.livebroadcast.application.dto.response;

import org.springframework.data.domain.Page;

import java.util.List;

public record BroadcastProductPageResponse(
        List<BroadcastProductListResponseDto> products,
        PaginationMeta pagination
){
    public static BroadcastProductPageResponse from(Page<BroadcastProductListResponseDto> page) {
        return new BroadcastProductPageResponse(
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
