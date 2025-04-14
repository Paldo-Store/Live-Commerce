package com.live_commerce.product.product.application.service;

import com.live_commerce.product.product.application.dto.ProductCreateRequestDto;
import com.live_commerce.product.product.application.dto.ProductResponseDto;
import com.live_commerce.product.product.application.dto.ProductSummaryDto;
import com.live_commerce.product.product.application.dto.ProductUpdateRequestDto;
import com.live_commerce.product.product.application.mapper.ProductMapper;
import com.live_commerce.product.product.application.validation.CompanyValidator;
import com.live_commerce.product.product.application.validation.ProductValidator;
import com.live_commerce.product.product.domain.exception.ProductException;
import com.live_commerce.product.product.domain.model.Product;
import com.live_commerce.product.product.domain.repository.ProductRepository;
import com.live_commerce.product.product.infrastructure.client.CompanyClient;
import com.live_commerce.product.product.infrastructure.client.ExternalCompanyResponseDto;
import com.live_commerce.product.product.presentation.common.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ProductService {

    private final ProductRepository productRepository;
    private final ProductValidator productValidator;
    private final CompanyValidator companyValidator;

    @Transactional
    public ProductResponseDto createProduct(ProductCreateRequestDto requestDto) {
        companyValidator.validateExistsAndActiveOrThrow(requestDto.companyId());

        Product product = ProductMapper.createDtoToEntity(requestDto, requestDto.companyId());
        productRepository.save(product);

        return ProductMapper.entityToDto(product);
    }

    @Transactional(readOnly = true)
    public ProductResponseDto getProduct(UUID productId) {
        Product product = productValidator.validateAndFindProduct(productId);

        return ProductMapper.entityToDto(product);
    }

    @Transactional
    public ProductResponseDto updateProduct(UUID productId, ProductUpdateRequestDto requestDto) {
        Product product = productValidator.validateAndFindProduct(productId);

        product.update(requestDto);
        return ProductMapper.entityToDto(product);
    }

    @Transactional
    public void deleteProduct(UUID productId) {
        Product product = productValidator.validateAndFindProduct(productId);

        product.delete("temp");
    }



    public List<ProductSummaryDto> getProductsByIds(List<UUID> productIds) {
        return productRepository.findAllByProductIdInAndDeletedStatusFalse(productIds).stream()
                .map(ProductSummaryDto::fromEntity)
                .collect(Collectors.toList());
        // TODO: 추후 성능 고려하여 요청 개수 제한 로직 추가할 것
    }
}
