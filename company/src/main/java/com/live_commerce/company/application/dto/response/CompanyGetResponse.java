package com.live_commerce.company.application.dto.response;

import com.live_commerce.company.domain.model.Company;
import org.springframework.data.domain.Page;

import java.util.List;

public record CompanyGetResponse(List<CompanyCreateResponse> companies, int totalPages,
                                    long totalElements) {

    public static CompanyGetResponse of(Page<Company> companyPages) {
        return new CompanyGetResponse(companyPages.map(CompanyCreateResponse::of).toList(),
                companyPages.getTotalPages(), companyPages.getTotalElements());
    }
}
