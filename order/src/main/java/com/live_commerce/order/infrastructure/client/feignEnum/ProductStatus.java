package com.live_commerce.order.infrastructure.client.feignEnum;

public enum ProductStatus {
    PREPARING("판매예정"),
    SELLING("판매중"),
    SOLD_OUT("품절"),
    STOPPED("판매중지");

    private final String description;

    ProductStatus(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}
