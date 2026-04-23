package com.live_commerce.company.application.dto.request;

import com.live_commerce.company.domain.model.CompanyType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public record CompanyCreateRequest(
        @NotBlank(message = "업체이름은 필수입니다.") String name,
        @NotNull(message = "업체소유주 등록은 필수입니다.") UUID owner,
        @NotNull(message = "업체 타입은 필수입니다.") CompanyType type,
        @NotBlank(message = "업체주소는 필수입니다.") String address,
        @NotBlank(message = "업체번호는 필수입니다.") String number,
        @NotBlank(message = "업체설명은 필수입니다.") String description
) {
}
