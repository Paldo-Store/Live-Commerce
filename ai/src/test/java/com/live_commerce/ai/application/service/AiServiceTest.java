package com.live_commerce.ai.application.service;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.util.List;
import java.util.UUID;
import java.util.stream.IntStream;

import com.live_commerce.ai.application.dto.request.AiAnalyzeRequestDto;
import com.live_commerce.ai.application.dto.response.AiCreateResponseDto;
import com.live_commerce.ai.application.dto.response.AiGetResponseDto;
import com.live_commerce.ai.application.exception.AiExceptionCode;
import com.live_commerce.ai.application.exception.CustomException;
import com.live_commerce.ai.domain.model.AI;
import com.live_commerce.ai.domain.prompt.PromptGenerator;
import com.live_commerce.ai.domain.repository.AiRepository;
import com.live_commerce.ai.infrastructure.client.GeminiServiceAdapter;
import com.live_commerce.ai.infrastructure.security.RequestUserDetails;
import com.live_commerce.ai.infrastructure.slack.SlackSender;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class AiServiceTest {

	@Autowired
	private AiService aiService;
	@Autowired
	private AiRepository aiRepository;

	@MockitoBean
	private PromptGenerator promptGenerator;
	@MockitoBean
	private GeminiServiceAdapter geminiServiceAdapter;
	@MockitoBean
	private SlackSender slackSender;

	private final UUID broadcastId = UUID.randomUUID();
	private final RequestUserDetails master =
		new RequestUserDetails(UUID.randomUUID(), "master",  List.of(() -> "ROLE_MASTER"));
	private final RequestUserDetails customer =
		new RequestUserDetails(UUID.randomUUID(), "user",    List.of(() -> "ROLE_CUSTOMER"));


	@Test
	@DisplayName("내부 시크릿 불일치 → UNAUTHORIZED_INTERNAL_REQUEST 예외")
	void analyze_invalidSecret() {
		AiAnalyzeRequestDto dto = new AiAnalyzeRequestDto(
			broadcastId,
			new AiAnalyzeRequestDto.RequestPayload(List.of())
		);

		CustomException ex = catchThrowableOfType(
			() -> aiService.analyze(dto, "wrong-secret"),
			CustomException.class
		);

		assertThat(ex.getExceptionCode()).isEqualTo(AiExceptionCode.UNAUTHORIZED_INTERNAL_REQUEST);
	}

	@Test
	@DisplayName("50개 초과 메시지는 최근 50개만 사용")
	void analyze_success_withTrimming() {
		List<AiAnalyzeRequestDto.ChatMessage> messages = IntStream.range(0, 60)
			.mapToObj(i -> new AiAnalyzeRequestDto.ChatMessage("msg" + i))
			.toList();

		AiAnalyzeRequestDto dto = new AiAnalyzeRequestDto(
			broadcastId,
			new AiAnalyzeRequestDto.RequestPayload(messages)
		);

		when(promptGenerator.generate(any())).thenReturn("prompt");
		when(geminiServiceAdapter.generateText("prompt")).thenReturn("summary");

		AiCreateResponseDto res = aiService.analyze(dto, "valid-secret");

		assertThat(res.responsePayload()).isEqualTo("summary");
		verify(slackSender).sendMessage(eq("UXXXXXX"), contains("채팅 분석 완료"));

		assertThat(aiRepository.count()).isEqualTo(1);
		AI saved = aiRepository.findAll().get(0);
		assertThat(saved.getResponsePayload()).isEqualTo("summary");
	}

	@Test
	@DisplayName("권한 없는 유저 조회 → FORBIDDEN 예외")
	void getAiAnalysis_forbidden() {
		UUID id = UUID.randomUUID();

		CustomException ex = catchThrowableOfType(
			() -> aiService.getAiAnalysis(id, customer),
			CustomException.class);

		assertThat(ex.getExceptionCode()).isEqualTo(AiExceptionCode.FORBIDDEN);
	}

	@Test
	@DisplayName("마스터 조회 성공")
	void getAiAnalysis_master_success() {
		/* given: DB에 하나 저장 */
		AI ai = aiRepository.save(AI.of(broadcastId, "{}", "응답"));

		/* when */
		AiGetResponseDto dto = aiService.getAiAnalysis(ai.getId(), master);

		/* then */
		assertThat(dto.responsePayload()).isEqualTo("응답");
	}

	@Test
	@DisplayName("삭제 성공 - 마스터")
	void deleteAiAnalysis_success() {
		AI ai = aiRepository.save(AI.of(broadcastId,"{}","응답"));

		aiService.deleteAiAnalysis(ai.getId(), master);

		assertThat(ai.isDeletedStatus()).isTrue();
	}

	@Test
	@DisplayName("삭제 실패 - 권한 없음")
	void deleteAiAnalysis_forbidden() {
		AI ai = aiRepository.save(AI.of(broadcastId,"{}","응답"));

		CustomException ex = catchThrowableOfType(
			() -> aiService.deleteAiAnalysis(ai.getId(), customer),
			CustomException.class);

		assertThat(ex.getExceptionCode()).isEqualTo(AiExceptionCode.FORBIDDEN);
	}
}
