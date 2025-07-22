package com.live_commerce.product.product.application.dto;

import java.time.Duration;

public record LiveDiscountRequestDto (
        Integer discountPrice,
        Integer durationMinutes
) {

    public Duration toDuration() {
        return Duration.ofMinutes(durationMinutes);
    }
}
