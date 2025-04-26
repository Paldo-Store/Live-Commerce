package com.live_commerce.coupon.infrastructure.client;

import java.util.UUID;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "user", url = "${gateway.base-url}", path = "/api/v1")
public interface UserClient {

  @GetMapping("/users")
  UUID getUserId(@PathVariable UUID userId);
}
