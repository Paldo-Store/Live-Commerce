package com.live_commerce.order.application.service;

import com.live_commerce.order.application.dto.request.OrderCreateRequest;
import com.live_commerce.order.application.dto.response.OrderCreateResponse;
import com.live_commerce.order.application.exception.OrderException;
import com.live_commerce.order.domain.model.Order;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class OrderService {

    //주문 생성
    @Transactional
    public OrderCreateResponse createOrder(OrderCreateRequest request, String userId) {

        // [productClient] 상품 쪽으로 검증 요청 필요 (상품의 개수랑 상품 ID를 같이 넘김)
        // 재고가 없거나 상품이 없다면 Exception
        OrderProductResponse responseProduct = productClient.confirmProduct(
                request.getProductId(), request.getProductQuantity());
        if (responseProduct.isValid()) {
            throw new OrderException("해당 상품이 존재하지 않거나 재고가 충분하지 않습니다.", HttpStatus.BAD_REQUEST);
        }

        // 주문 생성
        Long productTotalPrice = responseProduct.getProductTotalPrice();
        Order order = request.toOrder(productTotalPrice, userId);

        // 4. 생성한 주문 저장
        Order savedOrder = orderRepository.save(order);


        return OrderCreateResponse.fromOrder(savedOrder);
    }


}
