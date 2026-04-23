package com.live_commerce.livebroadcast.application.validation;

import com.live_commerce.livebroadcast.domain.exception.LiveBroadcastException;
import com.live_commerce.livebroadcast.infrastructure.client.company.CompanyClient;
import com.live_commerce.livebroadcast.infrastructure.client.company.ExternalCompanyResponseDto;
import com.live_commerce.livebroadcast.presentation.common.ApiResponse;
import feign.FeignException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
@RequiredArgsConstructor
public class CompanyValidator {

    private final CompanyClient companyClient;

    public void validateExistsAndActiveOrThrow(UUID companyId) {
        try {
            ApiResponse<ExternalCompanyResponseDto> companyResponse = companyClient.getCompany(companyId);

            if (companyResponse.getData() == null) {
                throw LiveBroadcastException.forExternalCompanyNotFound();
            }

        } catch (FeignException.NotFound e) {
            throw LiveBroadcastException.forExternalCompanyNotFound();
        } catch (FeignException e) {
            throw new RuntimeException("업체 서비스 호출 실패", e);
        }
    }


}
