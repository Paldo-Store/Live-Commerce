package com.live_commerce.order.application.dto.response;

import com.live_commerce.order.infrastructure.client.ProductCategory;
import com.live_commerce.order.infrastructure.client.ProductStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
// product -> order
public class OrderProductResponse {
    private UUID productId;  //실제 상품 아이디
    private String productName;  //상품 이름
    private String productDescription;
    private Double productPrice;
    private ProductCategory productCategory;
    private ProductStatus productStatus;
    private UUID companyId;
    private Boolean soldOut;  //상품 품절 여부
}
