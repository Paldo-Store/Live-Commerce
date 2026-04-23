package com.live_commerce.ai.application.service;

import java.util.List;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.live_commerce.ai.application.dto.request.AiAnalyzeRequestDto;
import com.live_commerce.ai.application.dto.request.AiSearchCondition;
import com.live_commerce.ai.application.dto.response.AiCreateResponseDto;
import com.live_commerce.ai.application.dto.response.AiGetResponseDto;
import com.live_commerce.ai.application.exception.AiExceptionCode;
import com.live_commerce.ai.application.exception.CustomException;
import com.live_commerce.ai.domain.model.AI;
import com.live_commerce.ai.domain.repository.AiRepository;
import com.live_commerce.ai.domain.prompt.PromptGenerator;
import com.live_commerce.ai.infrastructure.client.GeminiServiceAdapter;
import com.live_commerce.ai.infrastructure.security.RequestUserDetails;
import com.live_commerce.ai.infrastructure.slack.SlackSender;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AiService {

	private final AiRepository aiRepository;
	private final PromptGenerator promptGenerator;
	private final GeminiServiceAdapter geminiServiceAdapter;
	private final ObjectMapper objectMapper;
	private final SlackSender slackSender;

	private static final int MAX_CHAT_MESSAGES = 50;

	@Value("${internal.secret}")
	private String internalSecret;
	@Value("${slack.admin-user-id}")
	private String adminSlackUserId;


	public AiCreateResponseDto analyze(AiAnalyzeRequestDto request, String providedSecret) {
		if (!internalSecret.equals(providedSecret)) {
			throw new CustomException(AiExceptionCode.UNAUTHORIZED_INTERNAL_REQUEST);
		}

		List<AiAnalyzeRequestDto.ChatMessage> messages = request.request_payload().chat_messages();

		List<AiAnalyzeRequestDto.ChatMessage> trimmed = messages.size() > MAX_CHAT_MESSAGES
			? messages.subList(messages.size() - MAX_CHAT_MESSAGES, messages.size())
			: messages;

		String prompt = promptGenerator.generate(trimmed);
		String response = generateResponse(prompt);
		String requestPayloadJson = serializeRequest(request);

		// TODO: 현재는 관리자용 슬랙 ID로만 알림 전송함
		//       추후에 쇼호스트 계정으로 전송 기능 구현 예정
		slackSender.sendMessage(adminSlackUserId,
			"채팅 분석 완료\n\n" + response);

		AI saved = aiRepository.save(AI.of(request.live_broadcast_id(), requestPayloadJson, response));
		return AiCreateResponseDto.from(saved);
	}

	@Transactional(readOnly = true)
	public AiGetResponseDto getAiAnalysis(UUID id, RequestUserDetails userDetails) {
		validateAiReadPermission(userDetails);
		AI ai = findAiById(id);
		return AiGetResponseDto.from(ai);
	}

	@Transactional(readOnly = true)
	public Page<AiGetResponseDto> getAiAnalysisList(AiSearchCondition condition, Pageable pageable, RequestUserDetails userDetails) {
		validateAiReadPermission(userDetails);
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

	@Transactional
	public void deleteAiAnalysis(UUID id, RequestUserDetails userDetails) {
		validateAiDeletePermission(userDetails);
		AI ai = findAiById(id);
		ai.markAsDeleted(userDetails.getUsername());
	}

	private String generateResponse(String prompt) {
		try {
			return geminiServiceAdapter.generateText(prompt);
		} catch (Exception e) {
			throw new CustomException(AiExceptionCode.GEMINI_API_ERROR);
		}
	}

	private String serializeRequest(AiAnalyzeRequestDto request) {
		try {
			return objectMapper.writeValueAsString(request);
		} catch (Exception e) {
			throw new CustomException(AiExceptionCode.SERIALIZATION_ERROR);
		}
	}

	private void validateAiReadPermission(RequestUserDetails userDetails) {
		if (!hasMasterRole(userDetails)) {
			throw new CustomException(AiExceptionCode.FORBIDDEN);
		}
	}

	private void validateAiDeletePermission(RequestUserDetails userDetails) {
		if (!hasMasterRole(userDetails)) {
			throw new CustomException(AiExceptionCode.FORBIDDEN);
		}
	}

	private boolean hasMasterRole(RequestUserDetails userDetails) {
		return userDetails.getAuthorities().stream()
			.anyMatch(auth -> auth.getAuthority().equals("ROLE_MASTER"));
	}

	private AI findAiById(UUID id) {
		return aiRepository.findById(id)
			.orElseThrow(() -> new CustomException(AiExceptionCode.ANALYSIS_NOT_FOUND));
	}




}
