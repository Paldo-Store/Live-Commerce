package com.live_commerce.notification.application.alert;

import java.util.UUID;
import org.springframework.stereotype.Component;

@Component
public class ConsoleAlertSender implements AlertSender {


  @Override
  public void send(UUID userId, String message){
    System.out.println("🔔 [TEST] userId = " + userId + ", message = \"" + message + "\"");
  }
}
