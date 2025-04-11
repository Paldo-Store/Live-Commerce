package com.live_commerce.product.inventory.domain.exception;

import com.live_commerce.product.inventory.application.exception.InventoryExceptionCode;
import com.live_commerce.product.product.application.exception.CustomException;

public class InventoryException extends CustomException {
    public InventoryException(InventoryExceptionCode code) {
        super(code);
    }

    public static void forInventoryNotFound() {
        throw new InventoryException(InventoryExceptionCode.NOT_FOUND);
    }

    public static void forProductNotFound() {
        throw new InventoryException(InventoryExceptionCode.PRODUCT_NOT_FOUND);
    }
}
