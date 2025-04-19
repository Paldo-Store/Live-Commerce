package com.live_commerce.notification.infrastructure.client.liveBroadcast;

import com.live_commerce.notification.presentation.dto.request.BroadcastNotificationContext;
import java.util.UUID;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "livebroadcast")
public interface LiveBroadcastClient {

  @GetMapping("/api/v1/livebroadcasts/{broadcastId}/subscribers")
  BroadcastNotificationContext getSubscribersWithName(@PathVariable UUID broadcastId);
}
