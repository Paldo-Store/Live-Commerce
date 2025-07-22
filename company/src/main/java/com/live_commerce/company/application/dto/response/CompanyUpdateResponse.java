package com.live_commerce.company.application.dto.response;

import com.live_commerce.company.domain.model.Company;
import com.live_commerce.company.domain.model.CompanyType;

import java.util.UUID;

public record CompanyUpdateResponse (
        UUID companyId,
        String name,
        UUID owner,
        CompanyType type,
        String address,
        String number,
        String description
) {

    public static CompanyUpdateResponse of(Company company) {
        return new CompanyUpdateResponse(
                company.getId(),
                company.getName(),
                company.getOwner(),
                company.getType(),
                company.getAddress(),
                company.getNumber(),
                company.getDescription()
        );
    }
}