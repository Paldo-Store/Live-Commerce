package com.live_commerce.company.presentation.controller;


import com.live_commerce.company.application.dto.request.CompanyCreateRequest;
import com.live_commerce.company.application.dto.response.CompanyCreateResponse;
import com.live_commerce.company.application.service.CompanyService;
import com.live_commerce.company.infrastructure.common.ResponseUtil;
import com.live_commerce.company.presentation.common.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RequestMapping
@RestController
@RequiredArgsConstructor
public class CompanyController {

    private final CompanyService companyService;

    //업체 생성 API
    @PostMapping("")
    public ResponseEntity<ApiResponse<CompanyCreateResponse>> createCompany(
            @Valid @RequestBody final CompanyCreateRequest request,
            @RequestHeader(name = "X-User-Id") String userId,
            @RequestHeader(name = "X-User-Role") String role) {
        CompanyCreateResponse response = companyService.createCompany(request, userId, role);
        return ResponseUtil.success(response);
    }

    //업체 조회 API

    //업체 수정 API

    //업체 삭제 API
}
