package com.live_commerce.payment.domain.event;

import java.util.UUID;

public record PaymentReadyDomainEvent(UUID orderId, UUID paymentId) {}
