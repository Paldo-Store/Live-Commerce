package com.live_commerce.product.product.presentation.controller;

import com.live_commerce.product.product.application.service.ProductDiscountService;
import com.live_commerce.product.product.application.service.ProductRankingService;
import com.live_commerce.product.product.application.dto.*;
import com.live_commerce.product.product.application.service.ProductService;
import com.live_commerce.product.product.application.validation.ProductValidator;
import com.live_commerce.product.product.infrastructure.common.ResponseUtil;
import com.live_commerce.product.product.infrastructure.security.RequestUserDetails;
import com.live_commerce.product.product.presentation.common.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/products")
@RequiredArgsConstructor
public class ProductController {

    private final ProductService productService;
    private final ProductRankingService productRankingService;
    private final ProductValidator productValidator;
    private final ProductDiscountService productDiscountService;

    @PreAuthorize("hasAnyRole('MASTER','SELLER')")
    @PostMapping
    public ResponseEntity<ApiResponse<ProductCreateResponseDto>> createProduct(
            @RequestBody ProductCreateRequestDto requestDto,
            @AuthenticationPrincipal RequestUserDetails user
    ) {
        ProductCreateResponseDto responseDto = productService.createProduct(requestDto, user);
        return ResponseUtil.success(responseDto);
    }

    @GetMapping("/{productId}")
    public ResponseEntity<ApiResponse<ProductResponseDto>> getProduct(@PathVariable UUID productId) {
        ProductResponseDto responseDto = productService.getProduct(productId);
        return ResponseUtil.success(responseDto);
    }

    @PreAuthorize("hasAnyRole('MASTER','SELLER')")
    @PatchMapping("/{productId}")
    public ResponseEntity<ApiResponse<ProductResponseDto>> updateProduct(
            @PathVariable UUID productId,
            @RequestBody ProductUpdateRequestDto requestDto,
            @AuthenticationPrincipal RequestUserDetails user
    ) {
        ProductResponseDto responseDto = productService.updateProduct(productId, requestDto, user);
        return ResponseUtil.success(responseDto);
    }

    @PreAuthorize("hasAnyRole('MASTER','SELLER')")
    @DeleteMapping("/{productId}")
    public ResponseEntity<ApiResponse<String>> deleteProduct(
            @PathVariable UUID productId,
            @AuthenticationPrincipal RequestUserDetails user
    ) {
        productService.deleteProduct(productId, user);
        return ResponseUtil.success("상품이 삭제되었습니다.");
    }

    @GetMapping("/search")
    public ResponseEntity<ApiResponse<ProductPageResponseDto>> searchProducts(
            @ModelAttribute ProductSearchCondition condition,
            Pageable pageable
    ) {
        ProductPageResponseDto response = productService.searchProducts(condition, pageable);
        return ResponseUtil.success(response);
    }


    @PreAuthorize("hasRole('MASTER')")
    @PostMapping("/bulk")
    public ResponseEntity<ApiResponse<List<ProductSummaryDto>>> getProductByIds(
            @RequestBody List<UUID> productIds
    ) {
        List<ProductSummaryDto> result = productService.getProductsByIds(productIds);
        return ResponseUtil.success(result);
    }

    @GetMapping("/popular")
    public ResponseEntity<ApiResponse<List<PopularProductsResponseDto>>> getPopularProducts() {
        List<PopularProductsResponseDto> top10Products = productRankingService.getTop10PopularProducts();
        return ResponseUtil.success(top10Products);
    }

    @PreAuthorize("hasAnyRole('MASTER', 'SHOW_HOST')")
    @PostMapping("/{productId}/live-discount")
    public ResponseEntity<ApiResponse<String>> applyLiveDiscount(
            @PathVariable UUID productId,
            @RequestBody @Valid LiveDiscountRequestDto request,
            @AuthenticationPrincipal RequestUserDetails userDetails
    ){
        productValidator.validateProductExists(productId);

        productDiscountService.applyLiveDiscount(
                productId,
                request,
                userDetails
        );

        return ResponseUtil.success("할인이 적용되었습니다.");
    }

    /**
     * 사용자용 가격확인 api
     */
    @GetMapping("/{productId}/price")
    public ResponseEntity<ApiResponse<ProductPriceResponseDto>> getProductPrice(@PathVariable UUID productId) {
        ProductPriceResponseDto responseDto = productService.getProductPrice(productId);
        return ResponseUtil.success(responseDto);
    }

    /**
     * 주문용 가격확인 api
     */
    @GetMapping("/{productId}/price-for-order")
    public ResponseEntity<ApiResponse<ProductOrderPriceDto>> getPriceForOrder(@PathVariable UUID productId) {
        ProductOrderPriceDto dto = productService.getCurrentPriceForOrder(productId);
        return ResponseUtil.success(dto);
    }



}
