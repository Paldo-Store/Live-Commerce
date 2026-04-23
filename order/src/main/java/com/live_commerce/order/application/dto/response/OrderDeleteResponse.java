package com.live_commerce.order.application.dto.response;

import java.util.UUID;

public record OrderDeleteResponse(
        UUID orderId,
        String message
) {
    public static OrderDeleteResponse of(UUID orderId) {
        return new OrderDeleteResponse(orderId, "업체 삭제가 완료되었습니다.");
    }
}
