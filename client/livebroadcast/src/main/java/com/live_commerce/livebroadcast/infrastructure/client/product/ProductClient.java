package com.live_commerce.livebroadcast.infrastructure.client.product;

import com.live_commerce.livebroadcast.infrastructure.client.company.ExternalCompanyResponseDto;
import com.live_commerce.livebroadcast.infrastructure.config.FeignLogConfig;
import com.live_commerce.livebroadcast.presentation.common.ApiResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;
import java.util.UUID;

@FeignClient(name = "product", url = "http://localhost:19091", path = "/api/v1/products",
        configuration = FeignLogConfig.class)
public interface ProductClient {

    @GetMapping("/{productId}")
    ApiResponse<ExternalProductResponseDto> getProduct(@PathVariable("productId") UUID productId);

    @PostMapping("/bulk")
    ApiResponse<List<ProductSummaryDto>> getProducts(@RequestBody List<UUID> productIds);

}
