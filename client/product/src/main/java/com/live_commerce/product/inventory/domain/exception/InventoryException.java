package com.live_commerce.product.inventory.domain.exception;

import com.live_commerce.product.inventory.application.exception.InventoryExceptionCode;
import com.live_commerce.product.product.application.exception.CustomException;

public class InventoryException extends CustomException {
    public InventoryException(InventoryExceptionCode code) {
        super(code);
    }

    public static InventoryException forInventoryNotFound() {
        return new InventoryException(InventoryExceptionCode.NOT_FOUND);
    }

    public static InventoryException forProductNotFound() {
        return new InventoryException(InventoryExceptionCode.PRODUCT_NOT_FOUND);
    }

    public static InventoryException forInventoryOutOfStock() {
        return new InventoryException(InventoryExceptionCode.INVENTORY_OUT_OF_STOCK);
    }


}
