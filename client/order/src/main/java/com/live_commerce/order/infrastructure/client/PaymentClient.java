package com.live_commerce.order.infrastructure.client;

import com.live_commerce.order.infrastructure.client.response.PaymentSuccessResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.UUID;

@FeignClient(name = "payment")
public interface PaymentClient {

    /**
     * 결제 승인 요청
     * @param orderId 주문 ID
     * @param finalPaidPrice 최종 결제 금액 (쿠폰 적용 후 금액)
     * @return 결제 성공 응답
     */
    @PostMapping("/api/v1/payments/{orderId}/approve")
    PaymentSuccessResponse approvePayment(@PathVariable("orderId") UUID orderId,
                                          @RequestParam("finalPaidPrice") Long finalPaidPrice);

    /**
     * 결제 취소 요청
     * @param orderId 주문 ID
     * @param finalPaidPrice 최종 결제 금액 (쿠폰 적용 후 금액)
     * @return 결제 취소 응답
     */
    // 결제 취소 요청을 보냄 (order -> payment) 결제 취소하려는 해당 주문id와 최종 결제 금액을 payment로 보내줌
    @PostMapping("/api/v1/payments/{orderId}/cancel")
    void cancelPayment(@PathVariable("orderId") UUID orderId,
                       @RequestParam("finalPaidPrice") Long finalPaidPrice);
}