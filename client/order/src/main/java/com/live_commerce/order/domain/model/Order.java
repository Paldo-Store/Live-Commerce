package com.live_commerce.order.domain.model;


import com.live_commerce.order.application.exception.OrderException;
import com.live_commerce.order.presentation.common.BaseEntity;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Builder;
import lombok.NoArgsConstructor;
import org.springframework.http.HttpStatus;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

@Builder
@Entity
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "p_order")
public class Order extends BaseEntity {

    // 주문 ID
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    // 주문할 상품의 ID
    @Column(name = "product_id")
    private UUID productId;

    // 주문한 사용자
    @Column(name = "user_id")
    private UUID userId;

    // 주문할 상품의 수량
    @Column(name = "product_quantity")
    private Integer productQuantity;

    // 상품 총 가격 - 할인 적용 전, 상품 총 합산 금액
    @Column(name = "product_total_price")
    private Double productTotalPrice;

    // 요청 사항
    @Column(name = "requirement")
    private String requirement;

    // 주문 상태
    @Column(name = "status")
    @Enumerated(EnumType.STRING)
    private OrderStatus status;

    //주문한 해당 방송
    @Column(name = "broadcast_id")
    private UUID broadcastId;

    //쿠폰 id
    @Column(name = "coupon_id")
    private UUID couponId;

    // 쿠폰을 적용한 할인 후 최종 결제 예정 금액
    @Column(name = "final_paid_price")
    private double finalPaidPrice;

    //주문 수정 update 함수
    public void updateOrder(Order updateOrder) {
        if (updateOrder.getProductId() != null) {
            this.productId = updateOrder.getProductId();
        }
        if (updateOrder.getProductQuantity() != null) {
            this.productQuantity = updateOrder.getProductQuantity();
        }
        if (updateOrder.getProductTotalPrice() != null) {
            this.productTotalPrice = updateOrder.getProductTotalPrice();
        }
        if (updateOrder.getRequirement() != null) {
            this.requirement = updateOrder.getRequirement();
        }
        if (updateOrder.getStatus() != null) {
            this.status = updateOrder.getStatus();
        }
    }

    //주문 상태 변경 함수
    public void changeStatus(OrderStatus newStatus) {
        // 이미 취소된 주문이면 다시 변경 불가
        if (this.status == OrderStatus.CANCELLED) {
            throw new OrderException("이미 취소된 주문은 상태를 변경할 수 없습니다.", HttpStatus.BAD_REQUEST);
        }

        // 상태가 동일하다면 무시 (선택사항)
        if (this.status == newStatus) {
            throw new OrderException("변경하려는 상태가 현재 상태와 같습니다.", HttpStatus.BAD_REQUEST);
        }

        this.status = newStatus;
    }
}
