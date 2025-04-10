package com.live_commerce.product.application.service;

import com.live_commerce.product.application.dto.ProductCreateRequestDto;
import com.live_commerce.product.application.dto.ProductResponseDto;
import com.live_commerce.product.application.dto.ProductUpdateRequestDto;
import com.live_commerce.product.application.mapper.ProductMapper;
import com.live_commerce.product.domain.exception.ProductException;
import com.live_commerce.product.domain.model.Product;
import com.live_commerce.product.domain.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ProductService {

    private final ProductRepository productRepository;

    @Transactional
    public ProductResponseDto createProduct(ProductCreateRequestDto requestDto) {
        Product product = ProductMapper.createDtoToEntity(requestDto);
        productRepository.save(product);
        return ProductMapper.entityToDto(product);
    }

    @Transactional(readOnly = true)
    public ProductResponseDto getProduct(UUID id) {
        Product product = productRepository.findByIdAndDeletedStatusFalse(id).orElse(null);

        if (product == null) {
            ProductException.forProductNotFound();
        }

        return ProductMapper.entityToDto(product);
    }

    @Transactional
    public ProductResponseDto updateProduct(UUID id, ProductUpdateRequestDto requestDto) {
        Product product = productRepository.findByIdAndDeletedStatusFalse(id).orElse(null);

        if (product == null) {
            ProductException.forProductNotFound();
        }

        product.update(requestDto);
        return ProductMapper.entityToDto(product);
    }

    @Transactional
    public void deleteProduct(UUID id) {
        Product product = productRepository.findByIdAndDeletedStatusFalse(id).orElse(null);

        if (product == null) {
            ProductException.forProductNotFound();
            return;
        }

        product.delete("temp");
    }

}
