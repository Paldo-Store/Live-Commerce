package com.live_commerce.company.application.dto.request;

import com.live_commerce.company.domain.model.CompanyType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record CompanyUpdateRequest (
        @NotBlank(message = "업체 이름은 필수입니다.") String name,
        @NotNull(message = "업체 타입은 필수입니다.") CompanyType type,
        @NotBlank(message = "업체 주소는 필수입니다.") String address,
        @NotBlank(message = "업체 번호는 필수입니다.") String number,
        @NotBlank(message = "업체 설명은 필수입니다.") String description
) {
}
