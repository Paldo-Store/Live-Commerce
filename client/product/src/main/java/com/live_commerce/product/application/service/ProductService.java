package com.live_commerce.product.application.service;

import com.live_commerce.product.application.dto.ProductCreateRequestDto;
import com.live_commerce.product.application.dto.ProductResponseDto;
import com.live_commerce.product.application.dto.ProductUpdateRequestDto;
import com.live_commerce.product.application.mapper.ProductMapper;
import com.live_commerce.product.domain.exception.ProductException;
import com.live_commerce.product.domain.model.Product;
import com.live_commerce.product.domain.repository.ProductRepository;
import com.live_commerce.product.infrastructure.client.CompanyClient;
import com.live_commerce.product.infrastructure.client.ExternalCompanyResponseDto;
import com.live_commerce.product.presentation.common.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ProductService {

    private final ProductRepository productRepository;

    private final CompanyClient companyClient;

    @Transactional
    public ProductResponseDto createProduct(ProductCreateRequestDto requestDto) {
        ApiResponse<ExternalCompanyResponseDto> companyResponse = companyClient.getCompany(requestDto.companyId());
         // data 필드로 꺼내기
        if (companyResponse == null) {
            ProductException.forCompanyNotFound();
        }
        ExternalCompanyResponseDto companyResponseDto = companyResponse.getData();

        Product product = ProductMapper.createDtoToEntity(requestDto, companyResponseDto.companyId());
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
