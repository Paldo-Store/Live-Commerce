package com.live_commerce.chat.application.scheduler;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import org.redisson.api.RAtomicLong;
import org.redisson.api.RLock;
import org.redisson.api.RSet;
import org.redisson.api.RedissonClient;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.live_commerce.chat.application.dto.request.AiAnalyzeRequestDto;
import com.live_commerce.chat.application.dto.request.ChatAnalyzeRequestDto;
import com.live_commerce.chat.application.service.ChatService;
import com.live_commerce.chat.infrastructure.client.AiWebClient;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Mono;

@Slf4j
@Component
@RequiredArgsConstructor
public class ChatAnalyzeScheduler {

	private final RedissonClient redisson;
	private final ChatService chatService;
	private final AiWebClient aiWebClient;

	private static final int MIN_CHAT_COUNT = 20;
	private static final int MAX_CHAT_COUNT = 100;
	private static final int MIN_MESSAGE_LENGTH = 5;

	@Scheduled(fixedRate = 10 * 60 * 1000)
	public void analyzeIfNeeded() {
		log.info("[ChatScheduler] Redisson 기반 채팅 분석 시작");

		Set<String> activeBroadcastIds = new HashSet<>(redisson.getSet("chat:active"));
		if (activeBroadcastIds.isEmpty()) {
			log.info("[ChatScheduler] 활성 방송 없음");
			return;
		}

		activeBroadcastIds.forEach(id -> processBroadcast(UUID.fromString(id)));
	}

	private void processBroadcast(UUID broadcastId) {
		RAtomicLong counter = redisson.getAtomicLong("chat:count:" + broadcastId);
		if (!counter.isExists()) {
			redisson.getSet("chat:active").remove(broadcastId.toString());
			return;
		}

		String lockKey = "lock:ai:analyze:" + broadcastId;
		RLock lock = redisson.getLock(lockKey);
		boolean acquired = false;

		try {
			acquired = lock.tryLock(1, 10, TimeUnit.SECONDS);
			if (!acquired) {
				log.info("[ChatScheduler] 방송 {} - 락 획득 실패", broadcastId);
				return;
			}

			List<ChatAnalyzeRequestDto> chats = getValidChats(broadcastId);
			if (chats.size() < MIN_CHAT_COUNT) {
				log.info("[ChatScheduler] 방송 {} - 유효 채팅 부족 ({}개)", broadcastId, chats.size());
				return;
			}

			sendAiAnalyzeRequest(broadcastId, chats);

		} catch (Exception e) {
			log.error("[ChatScheduler] 방송 {} 처리 중 예외 발생", broadcastId, e);
		} finally {
			if (acquired && lock.isHeldByCurrentThread()) lock.unlock();
		}
	}

	private List<ChatAnalyzeRequestDto> getValidChats(UUID broadcastId) {
		LocalDateTime since = LocalDateTime.now().minusMinutes(10);
		return chatService.getChatsSince(broadcastId, since).stream()
			.filter(chat -> chat.message() != null && chat.message().trim().length() >= MIN_MESSAGE_LENGTH)
			.toList();
	}

	private void sendAiAnalyzeRequest(UUID broadcastId, List<ChatAnalyzeRequestDto> chats) {
		AiAnalyzeRequestDto request = new AiAnalyzeRequestDto(
			broadcastId,
			new AiAnalyzeRequestDto.RequestPayload(
				chats.stream()
					.map(chat -> new AiAnalyzeRequestDto.ChatMessage(chat.message()))
					.limit(MAX_CHAT_COUNT)
					.toList()
			)
		);

		aiWebClient.sendAiAnalyzeRequest(request)
			.timeout(Duration.ofSeconds(5))
			.onErrorResume(e -> {
				log.warn("[ChatScheduler] 방송 {} 분석 요청 실패: {}", broadcastId, e.getMessage());
				return Mono.empty();
			})
			.subscribe(v -> log.debug("[ChatScheduler] 방송 {} 분석 요청 성공", broadcastId));
	}

}
