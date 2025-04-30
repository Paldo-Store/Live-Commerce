package com.live_commerce.chat.infrastructure.redis;

import java.util.UUID;

import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import com.live_commerce.chat.application.dto.message.ChatRedisPayload;
import com.live_commerce.chat.application.dto.request.ChatCreateRequest;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class ChatRedisPublisher {
	private final RedisTemplate<String, Object> redisTemplate;

	public void publishChat(ChatCreateRequest request, UUID userId) {
		ChatRedisPayload payload = new ChatRedisPayload(userId, request);
		redisTemplate.convertAndSend("chat-channel", payload);
	}
}
