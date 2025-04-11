package com.live_commerce.ai.application.service;

import java.util.List;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.live_commerce.ai.application.dto.request.AiRequestDto;
import com.live_commerce.ai.application.dto.request.AiSearchCondition;
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
	// private final ChatClient chatClient;

	private static final int MAX_CHAT_MESSAGES = 50;

	public AiCreateResponseDto analyze(AiRequestDto request) {

		// TODO: 추후 chat-service 연동 시, 아래 부분을 FeignClient 호출로 대체할 예정
		// 현재는 임시로 AiRequestDto 내 chat_messages 직접 주입받음
		List<AiRequestDto.ChatMessage> messages = request.request_payload().chat_messages();

	/*
		[향후 구조]
		List<ChatMessagesResponse.ChatMessage> messages =
			chatClient.getChatMessages(request.live_broadcast_id()).content();

		List<String> trimmed = messages.size() > MAX_CHAT_MESSAGES
			? messages.subList(messages.size() - MAX_CHAT_MESSAGES, messages.size())
				.stream().map(ChatMessage::message).toList()
			: messages.stream().map(ChatMessage::message).toList();
	*/

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

	@Transactional(readOnly = true)
	public AiGetResponseDto getAiAnalysis(UUID id) {
		AI ai = aiRepository.findById(id)
			.orElseThrow(() -> new CustomException(AiExceptionCode.ANALYSIS_NOT_FOUND));

		return AiGetResponseDto.from(ai);
	}

	@Transactional(readOnly = true)
	public Page<AiGetResponseDto> getAiAnalysisList(AiSearchCondition condition, Pageable pageable) {
		int size = pageable.getPageSize();
		if (size != 10 && size != 30 && size != 50) {
			pageable = PageRequest.of(pageable.getPageNumber(), 10, pageable.getSort());
		}

		List<AI> ais = aiRepository.searchAi(condition);
		List<AiGetResponseDto> dtoList = ais.stream()
			.map(AiGetResponseDto::from)
			.toList();

		return new PageImpl<>(dtoList, pageable, dtoList.size());
	}


}
