package com.live_commerce.livebroadcast.application.validation;

import com.live_commerce.livebroadcast.domain.exception.LiveBroadcastException;
import com.live_commerce.livebroadcast.infrastructure.client.product.ExternalProductResponseDto;
import com.live_commerce.livebroadcast.infrastructure.client.product.ProductClient;
import com.live_commerce.livebroadcast.infrastructure.client.product.ProductSummaryDto;
import com.live_commerce.livebroadcast.presentation.common.ApiResponse;
import feign.FeignException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class ProductValidator {

    private final ProductClient productClient;

    public ExternalProductResponseDto getValidProductOrThrow(UUID productId) {
        System.out.println("🔍 ProductValidator.getValidProductOrThrow 호출됨, productId: " + productId);
        try {
            ApiResponse<ExternalProductResponseDto> productResponse = productClient.getProduct(productId);
            System.out.println("FeignClient 응답 수신 완료");
            if (productResponse.getData() == null) {
                System.out.println("상품 없음");
                throw LiveBroadcastException.forExternalProductNotFound();
            }

            return productResponse.getData();
        } catch (FeignException.NotFound e) {
            System.out.println("Feign 404 오류 발생");
            throw LiveBroadcastException.forExternalProductNotFound();
        } catch (FeignException e) {
            System.out.println("Feign 예외 발생: " + e.getMessage());
            throw new RuntimeException("상품 서비스 호출 실패", e);
        }
    }

    public List<ProductSummaryDto> getValidProductsOrThrow(List<UUID> productIds) {
        try {
            ApiResponse<List<ProductSummaryDto>> response = productClient.getProducts(productIds);
            List<ProductSummaryDto> data = response.getData();

            if (data == null || data.size() != productIds.size()) {
                throw LiveBroadcastException.forExternalProductNotFound(); // 일부 없을 수 있음
            }

            return data;
        } catch (FeignException.NotFound e) {
            throw LiveBroadcastException.forExternalProductNotFound();
        } catch (FeignException e) {
            throw new RuntimeException("상품 서비스 호출 실패", e);
        }
    }

}
