package com.live_commerce.product.product.application.validation;

import com.live_commerce.product.product.domain.exception.ProductException;
import com.live_commerce.product.product.infrastructure.client.CompanyClient;
import com.live_commerce.product.product.infrastructure.client.ExternalCompanyResponseDto;
import com.live_commerce.product.product.presentation.common.ApiResponse;
import feign.FeignException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
@RequiredArgsConstructor
public class CompanyValidator {

    private final CompanyClient companyClient;

    public void validateExistsOrThrow(UUID companyId) {
        getValidCompanyOrThrow(companyId);
    }

    public ExternalCompanyResponseDto getValidCompanyOrThrow(UUID companyId) {
        try {
            ApiResponse<ExternalCompanyResponseDto> response = companyClient.getCompany(companyId);
            ExternalCompanyResponseDto company = response.getData();

            if (company == null) {
                throw ProductException.forExternalCompanyNotFound();
            }

            return company;
        } catch (FeignException.NotFound e) {
            throw ProductException.forExternalCompanyNotFound();
        } catch (FeignException e) {
            throw new RuntimeException("업체 서비스 호출 실패", e);
        }
    }

}
