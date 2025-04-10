package com.live_commerce.product.infrastructure.client;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.UUID;



public record ExternalCompanyResponseDto (
        UUID companyId
) { }
