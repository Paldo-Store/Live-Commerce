package com.live_commerce.livebroadcast.application.validation;

import com.live_commerce.livebroadcast.domain.exception.LiveBroadcastException;
import com.live_commerce.livebroadcast.infrastructure.client.product.ExternalProductResponseDto;
import com.live_commerce.livebroadcast.infrastructure.client.product.ProductClient;
import com.live_commerce.livebroadcast.presentation.common.ApiResponse;
import feign.FeignException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
@RequiredArgsConstructor
public class ProductValidator {

    private final ProductClient productClient;

    public ExternalProductResponseDto getValidProductOrThrow(UUID productId) {
        try {
            ApiResponse<ExternalProductResponseDto> productResponse = productClient.getProduct(productId);

            if (productResponse.getData() == null) {
                throw LiveBroadcastException.forExternalProductNotFound();
            }

            return productResponse.getData();
        } catch (FeignException.NotFound e) {
            throw LiveBroadcastException.forExternalProductNotFound();
        } catch (FeignException e) {
            throw new RuntimeException("상품 서비스 호출 실패", e);
        }
    }
}
