package com.live_commerce.order.infrastructure;

import com.fasterxml.jackson.annotation.JsonProperty;

public record PaymentReadyResponseDto(
        String tid,
        @JsonProperty("next_redirect_pc_url")
        String nextRedirectUrl
) {
}
