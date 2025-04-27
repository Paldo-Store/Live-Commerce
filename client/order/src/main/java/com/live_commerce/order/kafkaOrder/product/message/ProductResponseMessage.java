package com.live_commerce.order.kafkaOrder.product.message;

import com.live_commerce.order.infrastructure.client.response.ProductCreateResponseDto;

import java.util.UUID;

//상품 응답 메시지
public record ProductResponseMessage(UUID productId, ProductCreateResponseDto productDetails) {}
