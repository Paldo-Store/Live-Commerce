package com.live_commerce.order.infrastructure.client;

import com.live_commerce.order.infrastructure.PaymentReadyResponseDto;
import com.live_commerce.order.infrastructure.client.response.PaymentSuccessResponse;
import com.live_commerce.order.presentation.common.ApiResponse;
import com.live_commerce.payment.application.dto.request.PaymentRefundResponseDto;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.UUID;

@FeignClient(name = "payment")
public interface PaymentClient {

    //결제 대기로 요청 전송
    @PostMapping("/ready")
    ApiResponse<PaymentReadyResponseDto> readyPayment(@RequestBody PaymentReadyRequestDto requestDto);

    //결제 환북 요청
    @PostMapping("/{orderId}/refund")
    ApiResponse<PaymentRefundResponseDto> refundPayment(@PathVariable("orderId") UUID orderId);
}