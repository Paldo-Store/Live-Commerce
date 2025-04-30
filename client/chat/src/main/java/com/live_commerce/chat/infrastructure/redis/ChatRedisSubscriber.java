package com.live_commerce.chat.infrastructure.redis;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.live_commerce.chat.application.dto.message.ChatRedisPayload;
import com.live_commerce.chat.application.service.ChatService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.connection.Message;
import org.springframework.data.redis.connection.MessageListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class ChatRedisSubscriber implements MessageListener {

	private final ChatService chatService;
	private final ObjectMapper objectMapper;

	@Override
	public void onMessage(Message message, byte[] pattern) {
		try {
			String body = new String(message.getBody());
			ChatRedisPayload payload = objectMapper.readValue(body, ChatRedisPayload.class);
			log.info("[Redis] 수신: {}", payload);
			chatService.createChat(payload.request(), payload.userId());
		} catch (Exception e) {
			log.error("Redis 메시지 처리 실패", e);
		}
	}
}
