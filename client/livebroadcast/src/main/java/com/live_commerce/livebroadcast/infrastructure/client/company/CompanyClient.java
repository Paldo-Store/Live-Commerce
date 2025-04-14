package com.live_commerce.livebroadcast.infrastructure.client.company;


import com.live_commerce.livebroadcast.presentation.common.ApiResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.UUID;

@FeignClient(name = "company")
public interface CompanyClient {

    @GetMapping("/api/v1/companies/{id}")
    ApiResponse<ExternalCompanyResponseDto> getCompany(@PathVariable("id") UUID id);
}
