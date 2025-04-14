package com.live_commerce.product.product.application.dto;

import org.springframework.data.domain.Page;

import java.util.List;

public record ProductPageResponseDto (
    List<ProductResponseDto> products,
    PaginationMeta pagination
){
    public static ProductPageResponseDto from(Page<ProductResponseDto> page) {
        return new ProductPageResponseDto(
                page.getContent(),
                new ProductPageResponseDto.PaginationMeta(
                        page.getNumber(),
                        page.getSize(),
                        page.getTotalPages(),
                        page.getTotalElements(),
                        page.isLast()
                )
        );
    }

    public record PaginationMeta (
            int currentPage,
            int pageSize,
            int totalPages,
            long totalElements,
            boolean isLast
    ) {}
}
