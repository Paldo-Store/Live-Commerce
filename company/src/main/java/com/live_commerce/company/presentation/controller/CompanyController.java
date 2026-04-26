package com.live_commerce.company.presentation.controller;

import com.live_commerce.company.application.dto.request.CompanyCreateRequest;
import com.live_commerce.company.application.dto.request.CompanyUpdateRequest;
import com.live_commerce.company.application.dto.response.CompanyCreateResponse;
import com.live_commerce.company.application.dto.response.CompanyDeleteResponse;
import com.live_commerce.company.application.dto.response.CompanyGetOneResponse;
import com.live_commerce.company.application.dto.response.CompanyGetResponse;
import com.live_commerce.company.application.dto.response.CompanyUpdateResponse;
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
    @PostMapping("")
    public ResponseEntity<ApiResponse<CompanyCreateResponse>> createCompany(
            @Valid @RequestBody final CompanyCreateRequest request,
            @AuthenticationPrincipal RequestUserDetails userDetails) {
        return ResponseUtil.success(companyService.createCompany(request, userDetails.getUserId(), userDetails.getRole()));
    }

    //업체 전체 조회 API
    //누구나 가능
    @GetMapping("")
    public ResponseEntity<ApiResponse<CompanyGetResponse>> getCompanies(
            @RequestParam final int page,
            @RequestParam final int size,
            @RequestParam(required = false) final String sort) {
        return ResponseUtil.success(companyService.getCompanies(page, size, sort));
    }

    //업체 단건 조회 API
    //누구나 가능
    @GetMapping("/{companyId}")
    public ResponseEntity<ApiResponse<CompanyGetOneResponse>> getCompany(
            @PathVariable final UUID companyId) {
        return ResponseUtil.success(companyService.getCompany(companyId));
    }

    //업체 이름 검색 API
    //누구나 가능
    @GetMapping("/search/{keyword}")
    public ResponseEntity<ApiResponse<CompanyGetResponse>> getCompaniesByKeyword(
            @PathVariable final String keyword,
            @RequestParam final int page,
            @RequestParam final int size,
            @RequestParam(required = false) final String sort) {
        return ResponseUtil.success(companyService.getCompaniesByKeyword(keyword, page, size, sort));
    }

    //업체 수정 API
    //MASTER과 업체 관리자만
    @PutMapping("/{companyId}")
    public ResponseEntity<ApiResponse<CompanyUpdateResponse>> updateCompany(
            @PathVariable final UUID companyId,
            @Valid @RequestBody CompanyUpdateRequest request,
            @AuthenticationPrincipal RequestUserDetails userDetails) {
        return ResponseUtil.success(companyService.updateCompany(companyId, request, userDetails.getUserId(), userDetails.getRole()));
    }

    //업체 삭제 API
    //MASTER과 업체 관리자만
    @DeleteMapping("/{companyId}")
    public ResponseEntity<ApiResponse<CompanyDeleteResponse>> deleteCompany(
            @PathVariable final UUID companyId,
            @AuthenticationPrincipal RequestUserDetails userDetails) {
        return ResponseUtil.success(companyService.deleteCompany(companyId, userDetails.getUserId(), userDetails.getRole()));
    }
}
