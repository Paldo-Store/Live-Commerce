package com.live_commerce.product.product.domain.exception;

import com.live_commerce.product.product.application.exception.CustomException;
import com.live_commerce.product.product.application.exception.ProductExceptionCode;

public class ProductException extends CustomException {
    public ProductException(ProductExceptionCode code) {
        super(code);
    }

    public static void forProductNotFound() {
        throw new ProductException(ProductExceptionCode.NOT_FOUND);
    }

    public static void forCompanyNotFound() {
        throw new ProductException(ProductExceptionCode.COMPANY_NOT_FOUND);
    }
}
