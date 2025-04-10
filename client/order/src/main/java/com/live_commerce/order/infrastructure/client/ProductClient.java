package com.live_commerce.order.infrastructure.client;

import com.live_commerce.order.application.dto.response.OrderProductResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.UUID;

@FeignClient(name = "product")
public interface ProductClient {

    @GetMapping("/api/v1/products/validate")
    OrderProductResponse getProduct(
            @RequestParam("productId") UUID productId,
            @RequestParam("quantity") int quantity);
}
