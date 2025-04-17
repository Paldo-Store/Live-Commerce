package com.live_commerce.order.application.service;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.live_commerce.order.application.dto.request.OrderStatusUpdateRequest;
import com.live_commerce.order.application.dto.response.OrderStatusUpdateResponse;
import com.live_commerce.order.application.exception.OrderException;
import com.live_commerce.order.application.exception.OrderExceptionCode;
import com.live_commerce.order.domain.model.Order;
import com.live_commerce.order.domain.model.OrderStatus;
import com.live_commerce.order.domain.repository.OrderRepository;
import com.live_commerce.order.infrastructure.PaymentReadyResponseDto;
import com.live_commerce.order.infrastructure.client.*;
import com.live_commerce.order.infrastructure.client.response.BroadcastStatusResponse;
import com.live_commerce.order.infrastructure.client.response.PaymentSuccessResponse;
import com.live_commerce.order.presentation.common.ApiResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
//결제 상태 변경
public class PaymentStatusTransitionService {
    private final ProductClient productClient;
    private final PaymentClient paymentClient;
    private final CouponClient couponClient;
    private final OrderRepository orderRepository;

    //결제 상태 변경
    public OrderStatusUpdateResponse updateCreator(UUID orderId, OrderStatusUpdateRequest request, UUID userId, String role){
        // 주문 조회 - 해당 주문 없으면 예외처리
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new OrderException(OrderExceptionCode.NOT_FOUND));
        log.info("주문 조회 성공");

        //기존의 원래 주문 상태
        OrderStatus currentStatus = order.getStatus();

        // 권한 검증 - 고객은 주문 상태를 변경할 수 없음
        if (role.equals("ROLE_CUSTOMER")) {
            throw new OrderException("고객은 주문 상태를 변경할 수 없습니다.", HttpStatus.FORBIDDEN);
        }

        // 새 주문 요청 상태 파싱 및 검증 - 바꾸려는 주문 상태(오타) 잘못들어오면 예외 발생
        OrderStatus newStatus;
        try {
            newStatus = OrderStatus.valueOf(request.status());  //요청받은 새로운 상태 변수에 담기
        } catch (IllegalArgumentException e) {
            throw new OrderException("잘못된 주문 상태입니다.", HttpStatus.BAD_REQUEST);
        }

        //결제 성공 : PENDING -> PAID
        //PaymentStatusTransitionService
        if ( (currentStatus==OrderStatus.PENDING) &&  (newStatus == OrderStatus.PAID)){
            log.info("상태를 결제 성공으로 바꾸는 로직으로 들어옴");
            this.updateOrderStatusToPaid(order, newStatus);  //같은 서비스라서 this 붙임

            //service 바로 종료
            return OrderStatusUpdateResponse.fromOrder(order);
        }

        //결제 취소 : PAID -> REFUNDED
        //RefundStatusTransitionService
        //PAID상태인 경우에만, 상품 상태 변경이 일어나 취소된다면 -> 재고 복구, 결제 취소 처리
        if ( (currentStatus==OrderStatus.PAID) &&  (newStatus == OrderStatus.CANCELLED)) {
            log.info("결제 취소 로직에 들어옴");
            
            // 1. [결제 취소 처리] 필요 시 결제 서비스 호출(주문 번호와 쿠폰 적용 후 최종 결제 금액을 payment로 보내준다.)
            ApiResponse<PaymentRefundResponseDto> responseRefund= paymentClient.refundPayment(order.getId());
            log.info("결제 취소 요청 들어감");

            if(responseRefund == null){
                throw new OrderException("결제 취소 요청 실패. 다시 시도하기", HttpStatus.BAD_REQUEST);
            }

            // 2. [재고 복구] 상품 서비스 호출 - 주문의 상품 id와 주문 상품 개수를 보내준다. 재고 복구는 Product에서 로직 처리
            productClient.increaseInventory(new InventoryIncreaseRequestDto(
                    order.getProductId(),
                    order.getProductQuantity()));
            log.info("결제 취소 후 재고 복구완료");
            log.info("결제 취소 완료");
        }

        //주문 접수 -> 주문 취소 : PENDING -> CANCELLED
        //그냥 통과.

        // 상태 변경 - 주문 취소는 상태 변경 불가, 같은 주문 상태 변경은 예외 발생
        order.changeStatus(newStatus);
        log.info("상태 변경 성공");
        return OrderStatusUpdateResponse.fromOrder(order);
    }

    //결제 성공 : PENDING -> PAID
    @Transactional
    public void updateOrderStatusToPaid(Order order, OrderStatus newStatus) {

        // 결제 대기 -> 승인 -> 결제 완료 요청
        // 주문 아이디와 최종 결제 금액을 payment로 보내줌 (order -> Panyemt)

        BigDecimal amount = BigDecimal.valueOf(order.getFinalPaidPrice());
        /////// 결제 대기 요청
        //결제 대기 feign요청 -> 결제 대기 결과값 응답으로 받기
        ApiResponse<PaymentReadyResponseDto> responseReady= paymentClient.readyPayment(
                new PaymentReadyRequestDto(order.getId(), amount, order.getProductId().toString()));

        if(responseReady == null){
            throw new OrderException("결제 대기 요청 실패. 다시 시도하기", HttpStatus.BAD_REQUEST);
        }
        
        //결제 대기 결과값 데이터 꺼내기
        PaymentReadyResponseDto responsePaymentReady = responseReady.getData();  
        
        //TODO 결제 대기 결과값 받아오기 tid, url 등등.
        String tidReady = responsePaymentReady.tid();
        String pgToken = null;
       
        log.info("결제 대기 로직 수행 완료");

        ////////// 결제 승인 요청
        //결제 승인 요청 ( order -> paymentClient.approvePayment)
        //결제 승인 요청을 feign으로 보낸 후, 그 결과값을 받아온다.
        ApiResponse<PaymentApproveResponseDto> responseApprove= paymentClient.approvePayment(
                        new PaymentApproveRequestDto(tidReady, pgToken, order.getId()));
        
        if(responseApprove == null){
            throw new OrderException("결제 승인 요청 실패. 다시 시도하기", HttpStatus.BAD_REQUEST);
        }
        
        //결제 승인 결과값 데이터 꺼내기
        PaymentApproveResponseDto responsePaymentApprove = responseApprove.getData();

        String tidApprove = responsePaymentApprove.tid();
        LocalDateTime approveTime = responsePaymentApprove.approvedAt();
        
        //3. 결제 승인 응답확인
        if (approveTime == null) {
            throw new OrderException("결제가 승인되지 못했습니다. 다시 시도해주세요", HttpStatus.BAD_REQUEST);
        }

        // 4. 결제 금액 일치 여부 확인
//        if (!order.getFinalPaidPrice().equals(response.finalPaidPrice())) {
//            throw new OrderException("결제 금액이 일치하지 않습니다.", HttpStatus.BAD_REQUEST);
//        }

        // 5. Pending -> PAID (결제 완료 상태로 변경)
        order.changeStatus(newStatus);

        // 6. 재고 감소
        productClient.decreaseInventory(new InventoryDecreaseRequestDto(order.getProductId(), order.getProductQuantity()));

        // 7. 쿠폰 사용 처리
        if (order.getCouponId() != null) {
            couponClient.useCoupon(order.getCouponId());
        }
    }
}
