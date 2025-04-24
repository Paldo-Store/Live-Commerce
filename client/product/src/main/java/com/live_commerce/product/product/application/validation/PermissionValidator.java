package com.live_commerce.product.product.application.validation;

import com.live_commerce.product.product.domain.exception.ProductException;
import com.live_commerce.product.product.domain.model.Product;
import com.live_commerce.product.product.domain.repository.ProductRepository;
import com.live_commerce.product.product.infrastructure.client.ExternalCompanyResponseDto;
import com.live_commerce.product.product.infrastructure.security.RequestUserDetails;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
@RequiredArgsConstructor
public class PermissionValidator {

    private final CompanyValidator companyValidator;

    public void validateOwnerOrMaster(RequestUserDetails user, Product product) {
        if (user.isMaster()) return;

        UUID companyId = product.getCompanyId();

        ExternalCompanyResponseDto company = companyValidator.getValidCompanyOrThrow(companyId);

        if(!user.getUserId().equals(company.owner())) {
            throw ProductException.accessDenied();
        }
    }

    public void validateOwnerOrMasterByCompanyId(RequestUserDetails user, UUID companyId) {
        if (user.isMaster()) return;

        ExternalCompanyResponseDto company = companyValidator.getValidCompanyOrThrow(companyId);

        if (!user.getUserId().equals(company.owner())) {
            throw ProductException.accessDenied();
        }
    }

}
