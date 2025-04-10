package com.live_commerce.order.application.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderProductResponse {
    private UUID productId;  //상품 아이디
    private String productName;  //상품 이름
    private Integer productQuantity; //실제 상품의 재고 수량
    private Long productTotalPrice;  //상품의 총 가격
    private Boolean deletedStatus;  //상품 삭제 여부
}
