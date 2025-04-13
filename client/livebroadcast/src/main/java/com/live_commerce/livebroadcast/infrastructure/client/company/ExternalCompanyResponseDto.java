package com.live_commerce.livebroadcast.infrastructure.client.company;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.UUID;

public record ExternalCompanyResponseDto (
        @JsonProperty("id") UUID companyId
) {}
