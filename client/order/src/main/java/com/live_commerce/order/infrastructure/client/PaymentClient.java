package com.live_commerce.order.infrastructure.client;

import com.live_commerce.order.infrastructure.PaymentReadyResponseDto;
import com.live_commerce.order.presentation.common.ApiResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.UUID;

@FeignClient(name = "payment" , url = "http://localhost:19091", path = "/api/v1/payments")
public interface PaymentClient {

    //결제 대기로 요청 전송
    @PostMapping("/ready")
    ApiResponse<PaymentReadyResponseDto> readyPayment(@RequestBody PaymentReadyRequestDto requestDto);

    //결제 요청으로 전송
    @PostMapping("/approve")
    ApiResponse<PaymentApproveResponseDto> approvePayment(@RequestBody PaymentApproveRequestDto requestDto);

    //결제 환불 요청
    @PostMapping("/{orderId}/refund")
    ApiResponse<PaymentRefundResponseDto> refundPayment(@PathVariable("orderId") UUID orderId);
}