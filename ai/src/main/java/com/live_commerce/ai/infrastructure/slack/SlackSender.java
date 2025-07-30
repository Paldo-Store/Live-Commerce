package com.live_commerce.ai.infrastructure.slack;

import java.util.Map;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

import org.springframework.http.HttpHeaders;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class SlackSender {

	@Value("${slack.bot-token}")
	private String botToken;

	private static final String SLACK_API_URL = "https://slack.com/api/chat.postMessage";

	public void sendMessage(String slackUserId, String text) {
		WebClient.create()
			.post()
			.uri(SLACK_API_URL)
			.header(HttpHeaders.AUTHORIZATION, "Bearer " + botToken)
			.header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
			.bodyValue(Map.of(
				"channel", slackUserId,
				"text", text
			))
			.retrieve()
			.bodyToMono(String.class)
			.subscribe(response -> {
				System.out.println("Slack 응답: " + response);
			});
	}
}

