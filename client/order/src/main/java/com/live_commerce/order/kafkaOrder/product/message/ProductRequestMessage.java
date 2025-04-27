package com.live_commerce.order.kafkaOrder.product.message;

import java.util.UUID;

//상품 요청 메시지
public record ProductRequestMessage(UUID productId) {}