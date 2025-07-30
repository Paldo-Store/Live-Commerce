package com.live_commerce.company.application.dto.response;

import java.time.LocalDateTime;
import java.util.UUID;

public record CompanyDeleteResponse(
        UUID companyId,
        String message
) {
    public static CompanyDeleteResponse of(UUID companyId) {
        return new CompanyDeleteResponse(companyId, "업체 삭제가 완료되었습니다.");
    }
}
