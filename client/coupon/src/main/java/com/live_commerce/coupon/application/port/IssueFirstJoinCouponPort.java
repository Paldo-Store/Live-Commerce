package com.live_commerce.coupon.application.port;

import java.util.UUID;

public interface IssueFirstJoinCouponPort {

  void publishFirstJoinEvent(UUID userId);
}