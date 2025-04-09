package com.live_commerce.order.application.dto.request;

import com.live_commerce.order.domain.model.Order;
import com.live_commerce.order.domain.model.OrderStatus;
import jakarta.validation.constraints.Min;

import java.util.UUID;

public record OrderCreateRequest (
    UUID productId,
    @Min(1) Integer productQuantity,  // 주문할 수량 - 최소 1개이상이어야 유효한 값. @Valid랑 같이써야함
    String requirement)
{
    //OrderCreateRequest(요청데이터)를 내부에서 쓸 도메인 객체 Order로 변환하는 메서드
    public Order toOrder(Long productTotalPrice, String userId) {
        return Order.builder()
                .productId(productId) //요청에서 받은 상품 id
                .userId(UUID.fromString(userId))
                .productQuantity(productQuantity) //요청에서 받은 수량
                .productTotalPrice(productTotalPrice)
                .requirement(requirement) //요청사항
                .status(OrderStatus.PENDING) //주문 초기 상태는 무조건 대기중
                .build();
    }
}
