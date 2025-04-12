package com.live_commerce.livebroadcast.infrastructure.client.product;

import com.live_commerce.livebroadcast.infrastructure.client.company.ExternalCompanyResponseDto;
import com.live_commerce.livebroadcast.presentation.common.ApiResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;
import java.util.UUID;

@FeignClient(name = "product")
public interface ProductClient {

    @GetMapping("/api/v1/products/{id}")
    ApiResponse<ExternalProductResponseDto> getProduct(@PathVariable("id") UUID id);

    @PostMapping("/api/v1/products/bulk")
    ApiResponse<List<ProductSummaryDto>> getProducts(@RequestBody List<UUID> productIds);

    @GetMapping("/api/v1/companies/{companyId}")
    ApiResponse<ExternalCompanyResponseDto> getCompany(@PathVariable("companyId") UUID companyId);

}
