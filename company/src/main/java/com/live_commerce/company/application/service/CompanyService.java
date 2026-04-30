package com.live_commerce.company.application.service;

import com.live_commerce.company.application.dto.request.CompanyCreateRequest;
import com.live_commerce.company.application.dto.request.CompanyUpdateRequest;
import com.live_commerce.company.application.dto.response.CompanyCreateResponse;
import com.live_commerce.company.application.dto.response.CompanyDeleteResponse;
import com.live_commerce.company.application.dto.response.CompanyGetOneResponse;
import com.live_commerce.company.application.dto.response.CompanyGetResponse;
import com.live_commerce.company.application.dto.response.CompanyUpdateResponse;
import com.live_commerce.company.application.exception.CompanyException;
import com.live_commerce.company.application.exception.CompanyExceptionCode;
import com.live_commerce.company.domain.model.Company;
import com.live_commerce.company.domain.repository.CompanyQueryRepository;
import com.live_commerce.company.domain.repository.CompanyRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class CompanyService {

    private static final String ROLE_MASTER = "ROLE_MASTER";
    private static final String ROLE_SELLER = "ROLE_SELLER";

    private final CompanyRepository companyRepository;
    private final CompanyQueryRepository companyQueryRepository;

    //업체 생성 Service
    @Transactional
    public CompanyCreateResponse createCompany(CompanyCreateRequest request, UUID userId, String role) {
        log.info("User role: {}", role);
        validateAuthorization(role);
        //업체 생성 저장
        Company company = new Company(request.name(), request.owner(), request.type(), request.address(), request.number(), request.description());
        return CompanyCreateResponse.of(companyRepository.save(company));
    }

    //업체 조회 Service
    @Transactional(readOnly = true)
    public CompanyGetResponse getCompanies(final int page, final int size, final String sort) {
        //업체 전체 조회
        return CompanyGetResponse.of(companyQueryRepository.findAll(getPageable(page, size, sort)));
    }

    //업체 단건 조회 service
    @Transactional(readOnly = true)
    public CompanyGetOneResponse getCompany(final UUID id) {
        Company company = companyRepository.findById(id)
                .orElseThrow(() -> new CompanyException(CompanyExceptionCode.NOT_FOUND));
        return CompanyGetOneResponse.of(company);
    }

    //업체 이름 검색 service
    @Transactional(readOnly = true)
    public CompanyGetResponse getCompaniesByKeyword(String keyword, int page, int size, String sort) {
        Pageable pageable = getPageable(page, size, sort);
        //업체 이름으로 필터링
        //만약 키워드가 null이 아니면서 키워드가 공백이 아닐 경우!
        if (keyword != null && !keyword.isBlank()) {
            return CompanyGetResponse.of(companyQueryRepository.getCompaniesByKeyword(pageable, keyword));
        }
        return CompanyGetResponse.of(companyQueryRepository.findAll(pageable));
    }

    //업체 수정 service
    @Transactional
    public CompanyUpdateResponse updateCompany(UUID companyId, CompanyUpdateRequest request, UUID userId, String role) {
        log.info("User role: {}", role);
        validateAuthorization(role);
        final Company company = companyRepository.findById(companyId)
                .orElseThrow(() -> new CompanyException(CompanyExceptionCode.NOT_FOUND));
        validateNotDeleted(company);
        company.update(request);
        return CompanyUpdateResponse.of(company);
    }

    //업체 삭제 service
    @Transactional
    public CompanyDeleteResponse deleteCompany(UUID companyId, UUID userId, String role) {
        log.info("User role: {}", role);
        validateAuthorization(role);
        final Company company = companyRepository.findById(companyId)
                .orElseThrow(() -> new CompanyException(CompanyExceptionCode.NOT_FOUND));
        validateNotDeleted(company);
        company.delete(userId.toString());
        return CompanyDeleteResponse.of(company.getId());
    }

    private void validateAuthorization(String role) {
        if (!ROLE_MASTER.equals(role) && !ROLE_SELLER.equals(role)) {
            throw new AccessDeniedException("업체 관리 권한이 없습니다.");
        }
    }

    private void validateNotDeleted(Company company) {
        if (Boolean.TRUE.equals(company.getDeletedStatus())) {
            throw new CompanyException(CompanyExceptionCode.ALREADY_DELETED);
        }
    }

    //페이징 함수
    private Pageable getPageable(final int page, final int size, final String sort) {
        if (sort == null || sort.isBlank()) {
            return PageRequest.of(page, size);
        }
        String[] sortParams = sort.split(",");
        List<Sort.Order> orders = new ArrayList<>();
        for (String param : sortParams) {
            String[] fieldAndDirection = param.trim().split(" ");
            if (fieldAndDirection.length != 2) {
                throw new IllegalArgumentException("Invalid sort parameter format. Expected 'field direction' (e.g., 'name asc').");
            }
            String field = fieldAndDirection[0].trim();
            String direction = fieldAndDirection[1].trim().toUpperCase();
            if (!direction.equals("ASC") && !direction.equals("DESC")) {
                throw new IllegalArgumentException("Invalid sort direction. Use 'asc' or 'desc'.");
            }
            orders.add(new Sort.Order(Sort.Direction.fromString(direction), field));
        }
        return PageRequest.of(page, size, Sort.by(orders));
    }
}
