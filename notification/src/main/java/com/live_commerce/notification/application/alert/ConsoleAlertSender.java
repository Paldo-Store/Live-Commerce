package com.live_commerce.notification.application.alert;

import java.util.UUID;
import org.springframework.stereotype.Component;

@Component
public class ConsoleAlertSender implements AlertSender {


  @Override
  public void send(UUID userId, String name, String broadcastName){
    String msg = "[TEST] " + name + "님, \"" + broadcastName + "\" 방송이 30분 후 시작됩니다.";

    System.out.println("🔔 [TEST] userId = " + userId + ", message = \"" + msg + "\"");
  }
}
