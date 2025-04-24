package com.live_commerce.product.product.application.validation;

import com.live_commerce.product.product.domain.exception.ProductException;
import com.live_commerce.product.product.domain.model.Product;
import com.live_commerce.product.product.domain.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
@RequiredArgsConstructor
public class ProductValidator {

    private final ProductRepository productRepository;

    public Product validateAndFindProduct(UUID productId) {
        System.out.println("validateAndFindProduct 호출됨: " + productId);
        return productRepository.findByProductIdAndDeletedStatusFalse(productId)
                .orElseThrow(ProductException::forProductNotFound);
    }

    public void validateProductExists(UUID productId) {
        if (!productRepository.existsByProductIdAndDeletedStatusFalse(productId)) {
            throw ProductException.forProductNotFound();
        }
    }


}
