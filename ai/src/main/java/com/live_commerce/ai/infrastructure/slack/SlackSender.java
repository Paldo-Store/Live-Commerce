package com.live_commerce.ai.infrastructure.slack;

import java.util.Map;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public class SlackSender {

	@Value("${slack.bot-token}")
	private String botToken;

	private static final String SLACK_API_URL = "https://slack.com/api/chat.postMessage";

	private final WebClient webClient;

	public SlackSender(WebClient.Builder webClientBuilder) {
		this.webClient = webClientBuilder.build();
	}

	public void sendMessage(String slackUserId, String text) {
		webClient.post()
			.uri(SLACK_API_URL)
			.header(HttpHeaders.AUTHORIZATION, "Bearer " + botToken)
			.header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
			.bodyValue(Map.of(
				"channel", slackUserId,
				"text", text
			))
			.retrieve()
			.bodyToMono(String.class)
			.subscribe(null, error -> log.warn("[Slack] 메시지 전송 실패: userId={}", slackUserId, error));
	}
}
