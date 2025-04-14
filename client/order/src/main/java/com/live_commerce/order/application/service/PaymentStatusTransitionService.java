package com.live_commerce.order.application.service;

import com.live_commerce.order.application.exception.OrderException;
import com.live_commerce.order.domain.model.Order;
import com.live_commerce.order.domain.model.OrderStatus;
import com.live_commerce.order.infrastructure.client.CouponClient;
import com.live_commerce.order.infrastructure.client.PaymentClient;
import com.live_commerce.order.infrastructure.client.ProductClient;
import com.live_commerce.order.infrastructure.client.response.PaymentSuccessResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class PaymentStatusTransitionService {

    private final ProductClient productClient;
    private final PaymentClient paymentClient;
    private final CouponClient couponClient;

    //결제 성공 : PENDING -> PAID
    public void updateOrderStatusToPaid(Order order, OrderStatus newStatus) {

        //TODO PRODUCTClient에서 상품명 (상품명)

        // 1. 총 상품 결제 금액 계산
        //TODO Product에서 총 상품 결제 금액계산 - 수정된 상품 개수로 총 상품 결제 금액 계산
        Long productTotalPrice = productClient.calculateProductTotalPrice(order.getProductId(), order.getProductQuantity());

        // 총 상품 결제 금액 Order Entity에 저장
        order.updateProductTotalPrice(productTotalPrice);

        // 2.  쿠폰 할인 적용 -> 금액 계산 (쿠폰이 있을 경우)
        Long finalPaidPrice = productTotalPrice;

        // ordr -> coupon : 주문에 저장된 couponId와 총 상품 합산 결과를 Coupon 측으로 보내준다.
        // 쿠폰이 있을 경우
        if (order.getCouponId() != null) {
            //TODO Coupon 영역에서 할인 계산을 진행한다.
            Long discount = couponClient.getDiscountAmount(order.getCouponId(), productTotalPrice);

            //만약 할인 쿠폰 금액이 결제 금액보다 클 경우, 할인금액은 총 상품 합산 금액으로 한다.
            if (discount > productTotalPrice) {
                discount = productTotalPrice;
            }

            //최종 결제 금액 계산
            finalPaidPrice = productTotalPrice - discount;
        }

        // 쿠폰 적용 후 최종 결제 금액 Order Entity 에 저장
        order.updateFinalPaidPrice(finalPaidPrice);

        // 2. 결제 승인 요청
        // 주문 아이디와 최종 결제 금액을 payment로 보내줌 (order -> Panyemt)
        //TODO Payment에서 결제 로직 수행 (결제 진행 -> 결제 준비) -> ready
        PaymentSuccessResponse response= paymentClient.approvePayment(order.getId(), order.getFinalPaidPrice());

        //3. 결제 성공 응답확인
        if (!response.success()) {
            throw new OrderException("결제가 성공하지 못했습니다. 다시 시도해주세요", HttpStatus.BAD_REQUEST);
        }

        // 4. 결제 금액 일치 여부 확인
        if (!finalPaidPrice.equals(response.finalPaidPrice())) {
            throw new OrderException("결제 금액이 일치하지 않습니다.", HttpStatus.BAD_REQUEST);
        }

        // 5. Pending -> PAID (결제 완료 상태로 변경)
        order.changeStatus(newStatus);

        // 6. 재고 감소
        //TODO Product에서 productId체크, 재고 >주문개수, 재고 감소, 총 결제금액 계산
        productClient.reduceProductQuantity(order.getProductId(), order.getProductQuantity());

        // 7. 쿠폰 사용 처리
        if (order.getCouponId() != null) {
            couponClient.markCouponAsUsed(order.getCouponId());
        }
    }
}
