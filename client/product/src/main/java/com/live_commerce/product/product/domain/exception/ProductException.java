package com.live_commerce.product.product.domain.exception;

import com.live_commerce.product.product.application.exception.CustomException;
import com.live_commerce.product.product.application.exception.ProductExceptionCode;

public class ProductException extends CustomException {
    public ProductException(ProductExceptionCode code) {
        super(code);
    }

    public static ProductException forProductNotFound() {
        return new ProductException(ProductExceptionCode.NOT_FOUND);
    }

    public static ProductException forExternalCompanyNotFound() {
        return new ProductException(ProductExceptionCode.EXTERNAL_COMPANY_NOT_FOUND);
    }

    public static ProductException accessDenied() {
        return new ProductException(ProductExceptionCode.ACCESS_DENIED);
    }

    public static ProductException exceedsMaxRequestLimit() {
        return new ProductException(ProductExceptionCode.EXCEEDS_MAX_REQUEST_LIMIT);
    }
}
