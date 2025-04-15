package com.live_commerce.chat.application.scheduler;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

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

	private final ChatService chatService;
	private final AiWebClient aiWebClient;

	private static final int MIN_CHAT_COUNT = 20;
	private static final int MAX_CHAT_COUNT = 100;
	private static final int MIN_MESSAGE_LENGTH = 5;

	@Scheduled(fixedRate = 10 * 60 * 1000)
	public void analyzeRecentChats() {
		log.info("[ChatScheduler] 최근 채팅 분석 시작");

		try {
			LocalDateTime now = LocalDateTime.now();
			LocalDateTime thirtyMinutesAgo = now.minusMinutes(10);

			List<ChatAnalyzeRequestDto> recentChats = chatService.getChatsSince(thirtyMinutesAgo).stream()
				.filter(chat ->
					chat.liveBroadcastId() != null &&
						chat.message() != null &&
						chat.message().trim().length() >= MIN_MESSAGE_LENGTH
				)
				.toList();

			if (recentChats.isEmpty()) {
				log.info("[ChatScheduler] 유효한 채팅 없음");
				return;
			}

			recentChats.stream()
				.collect(Collectors.groupingBy(ChatAnalyzeRequestDto::liveBroadcastId))
				.forEach((broadcastId, chats) -> {
					if (chats.size() < MIN_CHAT_COUNT) return;

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
						.timeout(java.time.Duration.ofSeconds(5))
						.onErrorResume(e -> {
							log.warn("[ChatScheduler] 방송 {} 분석 요청 실패: {}", broadcastId, e.getMessage());
							return Mono.empty();
						})
						.subscribe(v -> log.debug("[ChatScheduler] 방송 {} 분석 요청 성공", broadcastId));
				});

		} catch (Exception e) {
			log.error("[ChatScheduler] 전체 처리 중 예외 발생", e);
		}
	}
}
