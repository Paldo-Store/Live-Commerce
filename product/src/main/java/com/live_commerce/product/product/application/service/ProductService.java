package com.live_commerce.product.product.application.service;

import com.live_commerce.product.inventory.application.service.InventoryService;
import com.live_commerce.product.product.application.dto.*;
import com.live_commerce.product.product.application.mapper.ProductMapper;
import com.live_commerce.product.product.application.validation.CompanyValidator;
import com.live_commerce.product.product.application.validation.PermissionValidator;
import com.live_commerce.product.product.application.validation.ProductValidator;
import com.live_commerce.product.product.domain.exception.ProductException;
import com.live_commerce.product.product.domain.model.Product;
import com.live_commerce.product.product.domain.repository.ProductQueryRepository;
import com.live_commerce.product.product.domain.repository.ProductRepository;
import com.live_commerce.product.product.infrastructure.security.RequestUserDetails;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
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
    private final PermissionValidator permissionValidator;
    private final ProductDiscountCacheService productDiscountCacheService;

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
    public ProductCreateResponseDto createProduct(ProductCreateRequestDto requestDto, RequestUserDetails user) {
        companyValidator.validateExistsOrThrow(requestDto.companyId());

        permissionValidator.validateOwnerOrMasterByCompanyId(user, requestDto.companyId());

        Product product = ProductMapper.createDtoToEntity(requestDto, requestDto.companyId());
        productRepository.save(product);

        return ProductMapper.entityToCreateDto(product);
    }

    @Cacheable(value = "productDetail", key = "#productId")
    @Transactional(readOnly = true)
    public ProductResponseDto getProduct(UUID productId) {
        Product product = productValidator.validateAndFindProduct(productId);
        boolean soldOut = inventoryService.isSoldOut(product.getProductId());

        return ProductMapper.entityToDto(product, soldOut);
    }

    @CacheEvict(value = "productDetail", key = "#productId")
    @Transactional
    public ProductResponseDto updateProduct(UUID productId, ProductUpdateRequestDto requestDto, RequestUserDetails user) {
        Product product = productValidator.validateAndFindProduct(productId);
        permissionValidator.validateOwnerOrMaster(user, product);
        boolean soldOut = inventoryService.isSoldOut(product.getProductId());

        product.update(requestDto);
        return ProductMapper.entityToDto(product, soldOut);
    }

    @CacheEvict(value = "productDetail", key = "#productId")
    @Transactional
    public void deleteProduct(UUID productId, RequestUserDetails user) {
        Product product = productValidator.validateAndFindProduct(productId);
        permissionValidator.validateOwnerOrMaster(user, product);
        product.delete(user.getUserId());
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

    private static final int MAX_PRODUCT_IDS = 100;

    public List<ProductSummaryDto> getProductsByIds(List<UUID> productIds) {
        if (productIds.size() > MAX_PRODUCT_IDS) {
            throw ProductException.exceedsMaxRequestLimit();
        }

        return productRepository.findAllByProductIdInAndDeletedStatusFalse(productIds).stream()
                .map(ProductSummaryDto::fromEntity)
                .collect(Collectors.toList());
    }

    /**
     * 내부 상품정보 조회용
     */
    @Transactional(readOnly = true)
    public Product findProductEntity(UUID productId) {
        return productValidator.validateAndFindProduct(productId);
    }

    @Transactional(readOnly = true)
    public ProductPriceResponseDto getProductPrice(UUID productId) {
        Product product = productValidator.validateAndFindProduct(productId);

        Integer currentPrice = productDiscountCacheService.getDiscountPrice(productId).orElse(product.getPrice());

        boolean isDiscounted = !currentPrice.equals(product.getPrice());

        return new ProductPriceResponseDto(productId, currentPrice, isDiscounted);
    }

    @Transactional(readOnly = true)
    public ProductOrderPriceDto getCurrentPriceForOrder(UUID productId) {
        Product product = productValidator.validateAndFindProduct(productId);

        Integer currentPrice = productDiscountCacheService.getDiscountPrice(productId).orElse(product.getPrice());

        return new ProductOrderPriceDto(productId, currentPrice);
    }
}
