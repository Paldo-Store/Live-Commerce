package com.live_commerce.product.inventory.application.exception;


import com.live_commerce.product.product.application.exception.ExceptionCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum InventoryExceptionCode implements ExceptionCode {

  NOT_FOUND(HttpStatus.NOT_FOUND, "재고 정보가 없습니다."),
  PRODUCT_NOT_FOUND(HttpStatus.NOT_FOUND, "상품이 존재하지 않습니다."),
  INVENTORY_OUT_OF_STOCK(HttpStatus.CONFLICT, "재고가 부족합니다."),
  LOCK_ACQUISITION_FAILED(HttpStatus.TOO_MANY_REQUESTS, "현재 처리 중입니다. 잠시 후 다시 시도해주세요.");


  private final HttpStatus httpStatus;
  private final String message;

}