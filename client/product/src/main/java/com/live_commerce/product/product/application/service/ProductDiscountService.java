package com.live_commerce.product.product.application.service;

import com.live_commerce.product.product.application.dto.LiveDiscountRequestDto;
import com.live_commerce.product.product.application.validation.ProductValidator;
import com.live_commerce.product.product.domain.model.Product;
import com.live_commerce.product.product.domain.model.ProductDiscount;
import com.live_commerce.product.product.domain.repository.ProductDiscountRepository;
import com.live_commerce.product.product.domain.repository.ProductRepository;
import com.live_commerce.product.product.infrastructure.security.RequestUserDetails;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ProductDiscountService {

    private final ProductRepository productRepository;
    private final ProductDiscountRepository productDiscountRepository;
    private final ProductDiscountCacheService productDiscountCacheService;
    private final ProductValidator productValidator;


    public void applyLiveDiscount(UUID productId, @Valid LiveDiscountRequestDto request, RequestUserDetails userDetails) {
        Product product = productValidator.validateAndFindProduct(productId);

        validateDiscountPrice(product.getPrice(), request.discountPrice());

        LocalDateTime now = LocalDateTime.now();
        LocalDateTime endAt = now.plus(request.toDuration());

        ProductDiscount discount = ProductDiscount.builder()
                .product(product)
                .discountPrice(request.discountPrice())
                .startAt(now)
                .endAt(endAt)
                .appliedBy(userDetails.getUserId())
                .build();

        productDiscountRepository.save(discount);

        productDiscountCacheService.cacheDiscountPrice(productId, request.discountPrice(), request.toDuration());
    }

    private void validateDiscountPrice(Integer originalPrice, Integer discountPrice) {
        if (discountPrice >= originalPrice) {
            throw new IllegalArgumentException("할인 가격은 원래 가격보다 낮아야 합니다.");
        }
    }
}
