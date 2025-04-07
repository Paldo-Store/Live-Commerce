package com.live_commerce.company.presentation.controller;


import com.live_commerce.company.application.dto.request.CompanyCreateRequest;
import com.live_commerce.company.application.dto.response.CompanyCreateResponse;
import com.live_commerce.company.application.dto.response.CompanyGetResponse;
import com.live_commerce.company.application.service.CompanyService;
import com.live_commerce.company.infrastructure.common.ResponseUtil;
import com.live_commerce.company.presentation.common.ApiResponse;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

@RequestMapping("/api/v1/companies")
@RestController
@RequiredArgsConstructor
public class CompanyController {

    private final CompanyService companyService;

    //업체 생성 API
    @PostMapping("/")
    public ResponseEntity<ApiResponse<CompanyCreateResponse>> createCompany(
            @Valid @RequestBody final CompanyCreateRequest request) {

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String userId = authentication.getName(); // == username
        String role = authentication.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .findFirst()
                .orElseThrow(() -> new AccessDeniedException("권한이 없습니다."));

        CompanyCreateResponse response = companyService.createCompany(request, userId, role);
        return ResponseUtil.success(response);
    }

    //업체 조회 API
    @GetMapping("/getCompanies")
    public ResponseEntity<ApiResponse<CompanyGetResponse>> getCompanies (
            @RequestParam final int page,
            @RequestParam final int size,
            @RequestParam(required = false) final String sort) {
        CompanyGetResponse response = companyService.getCompanies(page, size, sort);
        return ResponseUtil.success(response);
    }

    //업체 수정 API

    //업체 삭제 API
}
