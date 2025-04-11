package com.live_commerce.ai.application.service;

import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.live_commerce.ai.application.dto.request.AiRequestDto;
import com.live_commerce.ai.application.dto.response.AiCreateResponseDto;
import com.live_commerce.ai.application.dto.response.AiGetResponseDto;
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

	public AiCreateResponseDto analyze(AiRequestDto request) {
		// TODO: 추후 chat-service 연동 시, 아래 부분을 FeignClient 호출로 대체할 예정
		// 현재는 임시로 AiRequestDto 내 chat_messages 직접 주입받음
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
		return AiCreateResponseDto.from(saved);
	}

	public AiGetResponseDto findAiAnalysisById(UUID id) {
		AI ai = aiRepository.findById(id)
			.orElseThrow(() -> new CustomException(AiExceptionCode.ANALYSIS_NOT_FOUND));

		return AiGetResponseDto.from(ai);
	}


}
