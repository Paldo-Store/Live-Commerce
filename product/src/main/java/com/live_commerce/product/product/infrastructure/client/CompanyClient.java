package com.live_commerce.product.product.infrastructure.client;

import com.live_commerce.product.product.presentation.common.ApiResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.UUID;

@FeignClient(name = "company", url = "${gateway.base-url}", path = "/api/v1/companies")
public interface CompanyClient {

    @GetMapping("/{companyId}")
    ApiResponse<ExternalCompanyResponseDto> getCompany(@PathVariable("companyId") UUID companyId);

}
