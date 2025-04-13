package com.live_commerce.livebroadcast.application.dto.response;

import org.springframework.data.domain.Page;

import java.util.List;

public record LiveBroadcastPageResponse (
        List<LiveBroadcastResponseDto> products,
        PaginationMeta pagination
){
    public static LiveBroadcastPageResponse from(Page<LiveBroadcastResponseDto> page) {
        return new LiveBroadcastPageResponse(
                page.getContent(),
                new LiveBroadcastPageResponse.PaginationMeta(
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

