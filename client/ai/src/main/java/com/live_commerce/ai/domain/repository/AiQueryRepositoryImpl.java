package com.live_commerce.ai.domain.repository;

import java.util.List;
import java.util.UUID;

import com.live_commerce.ai.application.dto.request.AiSearchCondition;
import com.live_commerce.ai.domain.model.AI;
import com.live_commerce.ai.domain.model.QAI;
import com.querydsl.core.BooleanBuilder;
import com.querydsl.jpa.impl.JPAQueryFactory;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class AiQueryRepositoryImpl implements AiQueryRepository {

	private final JPAQueryFactory queryFactory;

	@Override
	public List<AI> searchAi(AiSearchCondition condition) {
		QAI ai = QAI.aI;
		BooleanBuilder builder = new BooleanBuilder();

		if (condition.liveBroadcastId() != null) {
			builder.and(ai.liveBroadcastId.eq(condition.liveBroadcastId()));
		}
		if (condition.createdFrom() != null) {
			builder.and(ai.createdAt.goe(condition.createdFrom()));
		}
		if (condition.createdTo() != null) {
			builder.and(ai.createdAt.loe(condition.createdTo()));
		}
		builder.and(ai.deletedStatus.isFalse());

		return queryFactory
			.selectFrom(ai)
			.where(builder)
			.orderBy(ai.createdAt.desc())
			.fetch();
	}
}
