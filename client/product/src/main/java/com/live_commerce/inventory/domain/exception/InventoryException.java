package com.live_commerce.inventory.domain.exception;

import com.live_commerce.product.application.exception.CustomException;
import com.live_commerce.product.application.exception.ProductExceptionCode;

public class InventoryException extends CustomException {
    public InventoryException(ProductExceptionCode code) {
        super(code);
    }
}
