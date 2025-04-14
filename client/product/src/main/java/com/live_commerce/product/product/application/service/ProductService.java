package com.live_commerce.product.product.application.service;

import com.live_commerce.product.inventory.application.service.InventoryService;
import com.live_commerce.product.product.application.dto.*;
import com.live_commerce.product.product.application.mapper.ProductMapper;
import com.live_commerce.product.product.application.validation.CompanyValidator;
import com.live_commerce.product.product.application.validation.ProductValidator;
import com.live_commerce.product.product.domain.exception.ProductException;
import com.live_commerce.product.product.domain.model.Product;
import com.live_commerce.product.product.domain.repository.ProductQueryRepository;
import com.live_commerce.product.product.domain.repository.ProductRepository;
import com.live_commerce.product.product.infrastructure.client.CompanyClient;
import com.live_commerce.product.product.infrastructure.client.ExternalCompanyResponseDto;
import com.live_commerce.product.product.presentation.common.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.*;
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
    private final ProductQueryRepository productQueryRepository;
    private final InventoryService inventoryService;

    private static final List<Integer> ALLOWED_PAGE_SIZES = List.of(10, 30, 50);

    private Pageable adjustPageable(Pageable pageable) {
        int page = pageable.getPageNumber();
        int size = pageable.getPageSize();

        if (!ALLOWED_PAGE_SIZES.contains(size)) {
            size = 10; // 기본값으로 고정
        }

        return PageRequest.of(page, size, pageable.getSort());
    }

    @Transactional
    public ProductCreateResponseDto createProduct(ProductCreateRequestDto requestDto) {
        companyValidator.validateExistsAndActiveOrThrow(requestDto.companyId());

        Product product = ProductMapper.createDtoToEntity(requestDto, requestDto.companyId());
        productRepository.save(product);

        return ProductMapper.entityToCreateDto(product);
    }

    @Transactional(readOnly = true)
    public ProductResponseDto getProduct(UUID productId) {
        Product product = productValidator.validateAndFindProduct(productId);
        boolean soldOut = inventoryService.isSoldOut(product.getProductId());

        return ProductMapper.entityToDto(product, soldOut);
    }

    @Transactional
    public ProductResponseDto updateProduct(UUID productId, ProductUpdateRequestDto requestDto) {
        Product product = productValidator.validateAndFindProduct(productId);
        boolean soldOut = inventoryService.isSoldOut(product.getProductId());

        product.update(requestDto);
        return ProductMapper.entityToDto(product, soldOut);
    }

    @Transactional
    public void deleteProduct(UUID productId) {
        Product product = productValidator.validateAndFindProduct(productId);

        product.delete("temp");
    }

    @Transactional(readOnly = true)
    public ProductPageResponseDto searchProducts(ProductSearchCondition condition, Pageable pageable) {

        Pageable adjustedPageable = adjustPageable(pageable);

        Page<Product> productPage = productQueryRepository.search(condition, adjustedPageable);

        List<ProductResponseDto> dtoList = productPage.getContent().stream()
                .map(product -> {
                    boolean soldOut = inventoryService.isSoldOut(product.getProductId());
                    return ProductMapper.entityToDto(product, soldOut);
                })
                .toList();

        Page<ProductResponseDto> page = new PageImpl<>(dtoList, adjustedPageable, productPage.getTotalElements());

        return ProductPageResponseDto.from(page);
    }


    public List<ProductSummaryDto> getProductsByIds(List<UUID> productIds) {
        return productRepository.findAllByProductIdInAndDeletedStatusFalse(productIds).stream()
                .map(ProductSummaryDto::fromEntity)
                .collect(Collectors.toList());
        // TODO: 추후 성능 고려하여 요청 개수 제한 로직 추가할 것
    }
}
