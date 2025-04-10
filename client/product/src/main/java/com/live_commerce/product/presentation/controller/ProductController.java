package com.live_commerce.product.presentation.controller;

import com.live_commerce.product.application.dto.ProductCreateRequestDto;
import com.live_commerce.product.application.dto.ProductResponseDto;
import com.live_commerce.product.application.dto.ProductUpdateRequestDto;
import com.live_commerce.product.application.service.ProductService;
import com.live_commerce.product.domain.repository.ProductRepository;
import com.live_commerce.product.infrastructure.common.ResponseUtil;
import com.live_commerce.product.presentation.common.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RefreshScope
@RestController
@RequestMapping("/api/v1/products")
@RequiredArgsConstructor
public class ProductController {

    private final ProductService productService;

    @PostMapping
    public ResponseEntity<ApiResponse<ProductResponseDto>> createProduct(@RequestBody ProductCreateRequestDto requestDto) {
        ProductResponseDto responseDto = productService.createProduct(requestDto);
        return ResponseUtil.success(responseDto);
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<ProductResponseDto>> getProduct(@PathVariable UUID id) {
        ProductResponseDto responseDto = productService.getProduct(id);
        return ResponseUtil.success(responseDto);
    }

    @PatchMapping("/{id}")
    public ResponseEntity<ApiResponse<ProductResponseDto>> updateProduct(@PathVariable UUID id, @RequestBody ProductUpdateRequestDto requestDto) {
        ProductResponseDto responseDto = productService.updateProduct(id, requestDto);
        return ResponseUtil.success(responseDto);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<String>> deleteProduct(@PathVariable UUID id) {
        productService.deleteProduct(id);
        return ResponseUtil.success("상품이 삭제되었습니다.");
    }
}
