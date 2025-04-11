package com.live_commerce.ai.application.service;

import java.util.List;

import org.springframework.stereotype.Service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.live_commerce.ai.application.dto.request.AiRequestDto;
import com.live_commerce.ai.application.dto.response.AiResponseDto;
import com.live_commerce.ai.application.exception.AiExceptionCode;
import com.live_commerce.ai.application.exception.CustomException;
import com.live_commerce.ai.domain.model.AI;
import com.live_commerce.ai.domain.repository.AiRepository;
import com.live_commerce.ai.domain.prompt.PromptGenerator;
import com.live_commerce.ai.infrastructure.client.GeminiServiceAdapter;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AiService {

	private final AiRepository aiRepository;
	private final PromptGenerator promptGenerator;
	private final GeminiServiceAdapter geminiServiceAdapter;
	private final ObjectMapper objectMapper;

	private static final int MAX_CHAT_MESSAGES = 50;

	public AiResponseDto analyze(AiRequestDto request) {
		List<AiRequestDto.ChatMessage> messages = request.request_payload().chat_messages();
		List<AiRequestDto.ChatMessage> trimmed = messages.size() > MAX_CHAT_MESSAGES
			? messages.subList(messages.size() - MAX_CHAT_MESSAGES, messages.size())
			: messages;

		String prompt = promptGenerator.generate(trimmed);
		String response = geminiServiceAdapter.generateText(prompt);

		String requestPayloadJson;
		try {
			requestPayloadJson = objectMapper.writeValueAsString(request);
		} catch (Exception e) {
			throw new CustomException(AiExceptionCode.SERIALIZATION_ERROR);
		}

		AI saved = aiRepository.save(AI.of(request.live_broadcast_id(), requestPayloadJson, response));
		return AiResponseDto.from(saved);

	}
}
