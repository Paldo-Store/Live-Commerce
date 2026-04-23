package com.live_commerce.coupon.application.port;


import java.util.UUID;

import org.springframework.stereotype.Component;

@Component
public interface PublishCouponUsedEventPort {
  void publishCouponUsedEvent(UUID couponId, UUID userId);
}
