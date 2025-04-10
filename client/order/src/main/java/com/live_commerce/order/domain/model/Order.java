package com.live_commerce.order.domain.model;


import com.live_commerce.order.presentation.common.BaseEntity;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Builder;
import lombok.NoArgsConstructor;

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

    // 상품 총 가격
    @Column(name = "product_total_price")
    private Long productTotalPrice;

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

    //주문 update
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
}
