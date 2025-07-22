package com.live_commerce.order.infrastructure.client.response;

public record PaymentSuccessResponse(boolean success, String message, Long finalPaidPrice) {
}

