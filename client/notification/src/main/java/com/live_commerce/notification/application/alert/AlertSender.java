package com.live_commerce.notification.application.alert;

import java.util.UUID;

public interface AlertSender {
  void send(UUID userId, String message);
}
