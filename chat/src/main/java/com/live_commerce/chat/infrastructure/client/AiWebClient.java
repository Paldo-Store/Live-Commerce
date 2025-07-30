package com.live_commerce.chat.infrastructure.client;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

import com.live_commerce.chat.application.dto.request.AiAnalyzeRequestDto;

import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Mono;

@Slf4j
@Component
public class AiWebClient {

	private final WebClient webClient;
	private final String internalSecret;

	public AiWebClient(
		@Value("${ai-service.url}") String baseUrl,
		@Value("${internal.secret}") String internalSecret,
		WebClient.Builder builder
	) {
		this.webClient = builder.baseUrl(baseUrl).build();
		this.internalSecret = internalSecret;
	}

	public Mono<Void> sendAiAnalyzeRequest(AiAnalyzeRequestDto requestDto) {
		return webClient.post()
			.uri("/api/v1/ai")
			.header("X-Internal-Secret", internalSecret)
			.bodyValue(requestDto)
			.retrieve()
			.bodyToMono(Void.class)
			.doOnSuccess(v -> log.info("AI 분석 요청 성공"))
			.doOnError(e -> log.error("AI 분석 요청 실패: {}", e.getMessage()));
	}
}



