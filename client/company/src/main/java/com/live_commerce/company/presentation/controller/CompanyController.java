package com.live_commerce.company.presentation.controller;


import com.live_commerce.company.application.dto.request.CompanyCreateRequest;
import com.live_commerce.company.application.dto.request.CompanyUpdateRequest;
import com.live_commerce.company.application.dto.response.*;
import com.live_commerce.company.application.service.CompanyService;
import com.live_commerce.company.infrastructure.common.ResponseUtil;
import com.live_commerce.company.infrastructure.security.RequestUserDetails;
import com.live_commerce.company.presentation.common.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RequestMapping("/api/v1/companies")
@RestController
@RequiredArgsConstructor
public class CompanyController {

    private final CompanyService companyService;

    //업체 생성 API
    //MASTER과 업체 관리자만
    @PostMapping("/")
    public ResponseEntity<ApiResponse<CompanyCreateResponse>> createCompany(
            @Valid @RequestBody final CompanyCreateRequest request,
            @AuthenticationPrincipal RequestUserDetails userDetails) {
        UUID userId = userDetails.getUserId();
        String role = userDetails.getAuthorities().iterator().next().getAuthority();
        CompanyCreateResponse response = companyService.createCompany(request, userId, role);
        return ResponseUtil.success(response);
    }

    //업체 전체 조회 API
    //누구나 가능
    @GetMapping("/")
    public ResponseEntity<ApiResponse<CompanyGetResponse>> getCompanies (
            @RequestParam final int page,
            @RequestParam final int size,
            @RequestParam(required = false) final String sort) {
        CompanyGetResponse response = companyService.getCompanies(page, size, sort);
        return ResponseUtil.success(response);
    }

    //업체 단건 조회 API
    //누구나 가능
    @GetMapping("/{companyId}")
    public ResponseEntity<ApiResponse<CompanyGetOneResponse>> getCompany (
            @PathVariable final UUID companyId) {
        CompanyGetOneResponse response = companyService.getCompany(companyId);
        return ResponseUtil.success(response);
    }

    //업체 이름 검색 API
    //누구나 가능
    @GetMapping("/search/{keyword}")
    public ResponseEntity<ApiResponse<CompanyGetResponse>> getCompaniesByKeyword (
            @PathVariable final String keyword,
            @RequestParam final int page,
            @RequestParam final int size,
            @RequestParam(required = false) final String sort){
        CompanyGetResponse response = companyService.getCompaniesByKeyword(keyword, page, size, sort);
        return ResponseUtil.success(response);
    }

    //업체 수정 API
    //MASTER과 업체 관리자만
    @PutMapping("/{companyId}")
    public ResponseEntity<ApiResponse<CompanyUpdateResponse>> updateCompany(
            @PathVariable final UUID companyId,
            @Valid @RequestBody CompanyUpdateRequest request,
            @AuthenticationPrincipal RequestUserDetails userDetails){
        UUID userId = userDetails.getUserId();
        String role = userDetails.getAuthorities().iterator().next().getAuthority();
        CompanyUpdateResponse response = companyService.updateCompany(companyId, request, userId, role);
        return ResponseUtil.success(response);
    }

    //업체 삭제 API
    //MASTER과 업체 관리자만
    @DeleteMapping("/{companyId}")
    public ResponseEntity<ApiResponse<CompanyDeleteResponse>> deleteCompany (
            @PathVariable final UUID companyId,
            @AuthenticationPrincipal RequestUserDetails userDetails){
        UUID userId = userDetails.getUserId();
        String role = userDetails.getAuthorities().iterator().next().getAuthority();
        CompanyDeleteResponse response = companyService.deleteCompany(companyId, userId, role);
        return ResponseUtil.success(response);
    }
}
