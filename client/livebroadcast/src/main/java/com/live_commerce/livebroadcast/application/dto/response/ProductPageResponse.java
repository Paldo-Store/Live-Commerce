package com.live_commerce.livebroadcast.application.dto.response;

import com.live_commerce.livebroadcast.infrastructure.client.product.ProductSummaryDto;
import org.springframework.data.domain.Page;

import java.util.List;

public record ProductPageResponse (
        List<BroadcastProductListResponseDto> products,
        PaginationMeta pagination
){
    public static ProductPageResponse from(Page<BroadcastProductListResponseDto> page) {
        return new ProductPageResponse(
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
