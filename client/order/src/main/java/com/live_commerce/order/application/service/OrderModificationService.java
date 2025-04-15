package com.live_commerce.order.application.service;

import com.live_commerce.order.application.dto.request.OrderUpdateRequest;
import com.live_commerce.order.application.dto.response.OrderProductResponse;
import com.live_commerce.order.application.dto.response.OrderUpdateResponse;
import com.live_commerce.order.application.exception.OrderException;
import com.live_commerce.order.application.exception.OrderExceptionCode;
import com.live_commerce.order.domain.model.Order;
import com.live_commerce.order.domain.model.OrderStatus;
import com.live_commerce.order.domain.repository.OrderRepository;
import com.live_commerce.order.infrastructure.client.ProductClient;
import com.live_commerce.order.presentation.common.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@RequiredArgsConstructor
@Service
public class OrderModificationService {

    private final OrderRepository orderRepository;
    private final ProductClient productClient;

    //주문 수정 service - 주문 상태 변경을 일어나지 않음.
    //주문 개수, 상품 id, 요청 사항만 수정 가능
    @Transactional
    public OrderUpdateResponse updateCreator(UUID orderId, OrderUpdateRequest request, UUID userId, String role) {

        //수정은 방송 종료 후에도 가능
        //단, 상품 id는 변경 불가

        // orderId에 해당하는 주문 가져오기
        Order order  = orderRepository.findById(orderId).orElseThrow(()
                -> new OrderException(OrderExceptionCode.NOT_FOUND));

        //고객일 경우 본인의 결제 내역만 수정 가능하게
        if ("ROLE_CUSTOMER".equals(role) && !order.getUserId().equals(userId)) {
            throw new OrderException("고객은 자신의 주문만 수정할 수 있습니다.", HttpStatus.FORBIDDEN);
        }

        // 현재 주문의 상태가 PENDING일떄만 수정 가능!
        OrderStatus status = order.getStatus();
        if (status != OrderStatus.PENDING) {
            throw new OrderException("주문 내역을 수정할 수 없습니다.", HttpStatus.BAD_REQUEST);
        }

        // 상품 ID가 기존과 다를 경우 예외 발생 - 상품 id는 수정 불가. 수정하려면 주문 취소 후 다시 시도
        if (!order.getProductId().equals(request.productId())) {
            throw new OrderException("상품 ID는 수정할 수 없습니다. 만약 주문을 수정하고 싶다면 주문 취소 후 재주문해주세요.", HttpStatus.BAD_REQUEST);
        }

        // [productClient] 상품 쪽으로 검증 요청 필요 (상품의 개수랑 상품 ID를 같이 넘김)
        // 재고가 없거나 상품이 없다면 Exception
        //주문 요청 상품 id, update할 상품 주문 개수 -> product
//        OrderProductResponse responseProduct = productClient.getProduct(
//                request.productId(),
//                request.productQuantity());
        ApiResponse<OrderProductResponse> responseProduct = productClient.getProduct(request.productId()); //주문 요청 상품 id -> product
        OrderProductResponse productResponseByOrder = responseProduct.getData();

        //TODO PRODUCT로 아래 로직 이동시키기
        //productClient.validateOrderRequest(request.productId(), orderQty);
        //상품 존재 여부 확인
        //삭제 여부 확인 - 단종 여부
        //재고 수량 확인

        // TODO 이미 삭제(단종)된 상품일 경우
        // 상품이 품절일 경우
        if(productResponseByOrder.getSoldOut()){
            throw new OrderException("해당 상품은 품절된 상품입니다.", HttpStatus.BAD_REQUEST);
        }

        int orderQty = request.productQuantity() != null ? request.productQuantity() : 1; // 사용자가 수정을 요청한 상품의 수량 (요청 order -> product)
        int stock = productResponseByOrder.getProductQuantity(); // 실제 상품의 해당 재고 수량 (응답 product-> order)

        //상품의 재고 수량이 0이거나, or 주문한 상품 개수 > 상품 재고 개수
        if (stock == 0 || orderQty > stock) {
            throw new OrderException("해당 상품이 존재하지 않거나 재고가 충분하지 않습니다.", HttpStatus.BAD_REQUEST);
        }

        //총 합계 금액 수정
        Long productTotalPrice = responseProduct.getProductTotalPrice();

        //주문 수정
        Order updateOrder = request.toOrder(productTotalPrice);
        order.updateOrder(updateOrder);

        return OrderUpdateResponse.fromOrder(order);
    }
}
