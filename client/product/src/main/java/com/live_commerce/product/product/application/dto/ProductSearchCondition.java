package com.live_commerce.product.product.application.dto;

public record ProductSearchCondition (
        String keyword,
        ProductSortOption sort,
        SortOrder order
){
    public ProductSortOption sortOrDefault() {
        return sort == null ? ProductSortOption.createdAt : sort;
    }

    public SortOrder orderOrDefault() {
        return order == null ? SortOrder.desc : order;
    }
}
