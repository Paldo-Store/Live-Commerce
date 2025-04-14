package com.live_commerce.product.product.infrastructure.client;

import com.live_commerce.product.product.presentation.common.ApiResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.UUID;

@FeignClient(name = "company")
public interface CompanyClient {

    @GetMapping("/api/v1/companies/{id}")
    ApiResponse<ExternalCompanyResponseDto> getCompany(@PathVariable("id") UUID id);

//    @GetMapping("/api/v1/companies/{id}")
//    public ExternalCompanyResponseDto getCompany(@PathVariable UUID id) {
//        return service.getCompany(id);
//    }
}
