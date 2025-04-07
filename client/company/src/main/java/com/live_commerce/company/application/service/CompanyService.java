package com.live_commerce.company.application.service;

import com.live_commerce.company.application.dto.request.CompanyCreateRequest;
import com.live_commerce.company.application.dto.response.CompanyCreateResponse;
import com.live_commerce.company.domain.model.Company;
import com.live_commerce.company.domain.repository.CompanyRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class CompanyService {

    private final CompanyRepository companyRepository;

    //업체 생성 Service
    @Transactional
    public CompanyCreateResponse createCompany(CompanyCreateRequest request, String userId, String role) {
        //TODO 권한 검증 추가
        
        //업체 생성 저장
        Company company = new Company(request.name(), request.owner(), request.type(), request.address(), request.number());
        Company saved = companyRepository.save(company);
        return CompanyCreateResponse.of(saved);
    }
}
