package com.live_commerce.company.application.service;

import com.live_commerce.company.application.dto.request.CompanyCreateRequest;
import com.live_commerce.company.application.dto.request.CompanyUpdateRequest;
import com.live_commerce.company.application.dto.response.*;
import com.live_commerce.company.application.exception.CompanyException;
import com.live_commerce.company.application.exception.CompanyExceptionCode;
import com.live_commerce.company.domain.model.Company;
import com.live_commerce.company.domain.repository.CompanyQueryRepository;
import com.live_commerce.company.domain.repository.CompanyRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class CompanyService {

    private final CompanyRepository companyRepository;
    private final CompanyQueryRepository companyQueryRepository;

    //업체 생성 Service
    @Transactional
    public CompanyCreateResponse createCompany(CompanyCreateRequest request, String userId, String role) {
        //TODO 권한 검증 추가
        
        //업체 생성 저장
        Company company = new Company(request.name(), request.owner(), request.type(), request.address(), request.number(), request.description());
        Company saved = companyRepository.save(company);
        return CompanyCreateResponse.of(saved);
    }

    //업체 조회 Service
    @Transactional(readOnly = true)
    public CompanyGetResponse getCompanies(final int page, final int size, final String sort) {

        //업체 전체 조회
        Pageable pageable = getPageable(page, size, sort);
        return CompanyGetResponse.of(companyQueryRepository.findAll(pageable));
    }

    //페이징 함수
    private Pageable getPageable(final int page, final int size, final String sort) {
        if (sort == null || sort.isBlank()) {
            return PageRequest.of(page, size); // 기본 정렬 없음
        }
        String[] sortParams = sort.split(",");
        List<Sort.Order> orders = new ArrayList<>();
        for (String param : sortParams) {
            String[] fieldAndDirection = param.trim().split("[- ]"); // '-' 또는 ' '으로 구분
            if (fieldAndDirection.length != 2) {
                throw new IllegalArgumentException(
                        "Invalid sort parameter format. Expected 'field direction' (e.g., 'name asc').");
            }
            String field = fieldAndDirection[0].trim();
            String direction = fieldAndDirection[1].trim().toUpperCase();
            if (!direction.equals("ASC") && !direction.equals("DESC")) {
                throw new IllegalArgumentException("Invalid sort direction. Use 'asc' or 'desc'.");
            }
            Sort.Direction dir = Sort.Direction.fromString(direction);
            orders.add(new Sort.Order(dir, field));
        }
        Sort sortObj = Sort.by(orders);
        return PageRequest.of(page, size, sortObj);
    }

    //업체 단건 조회 service
    @Transactional(readOnly = true)
    public CompanyGetOneResponse getCompany(final UUID id) {
        Company company = companyRepository.findById(id).orElseThrow();
        return CompanyGetOneResponse.of(company);
    }


    //업체 이름 검색 service
    @Transactional(readOnly = true)
    public CompanyGetResponse getCompaniesByKeyword(String keyword, int page, int size, String sort) {
        Pageable pageable = getPageable(page, size, sort);

        //업체 이름으로 필터링
        if(keyword != null || !keyword.isBlank()) {
            return CompanyGetResponse.of(companyQueryRepository.getCompaniesByKeyword(pageable, keyword));
        }
        return CompanyGetResponse.of(companyQueryRepository.findAll(pageable));
    }


    //업체 수정 service
    @Transactional
    public CompanyUpdateResponse updateCompany(UUID companyId, CompanyUpdateRequest request, String userId, String role) {
        //TODO 권한 검증 추가

        final Company company = companyRepository.findById(companyId)
                .orElseThrow(() -> new CompanyException(CompanyExceptionCode.NOT_FOUND));
        company.update(request);
        return CompanyUpdateResponse.of(company);
    }

    //업체 삭제 service
    @Transactional
    public CompanyDeleteResponse  deleteCompany(UUID companyId, String userId, String role) {
        //TODO 권한 검증 추가

        final Company company = companyRepository.findById(companyId)
                .orElseThrow(() -> new CompanyException(CompanyExceptionCode.NOT_FOUND));
        company.delete(userId);
        return CompanyDeleteResponse.of(company.getId());
    }
}
