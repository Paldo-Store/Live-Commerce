package com.live_commerce.order.infrastructure.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.UUID;

@FeignClient(name = "payment")
public interface PaymentClient {

    //결제 취소 요청을 보냄 (order -> payment) 결제 취소하려는 해당 주문id를 payment로 보내줌
    @PostMapping("/api/payments/cancel") 
    void cancelPayment(@RequestParam("orderId") UUID orderId);
}