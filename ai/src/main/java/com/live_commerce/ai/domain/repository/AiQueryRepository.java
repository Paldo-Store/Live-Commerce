package com.live_commerce.ai.domain.repository;

import java.util.List;

import com.live_commerce.ai.application.dto.request.AiSearchCondition;
import com.live_commerce.ai.domain.model.AI;

public interface AiQueryRepository {
	List<AI> searchAi(AiSearchCondition condition);
}
