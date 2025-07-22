package com.live_commerce.livebroadcast.domain.repository.query;

import com.live_commerce.livebroadcast.application.dto.response.LiveBroadcastResponseDto;
import com.live_commerce.livebroadcast.domain.model.QLiveBroadcast;
import com.querydsl.core.types.Projections;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class LiveBroadcastQueryRepositoryImpl implements LiveBroadcastQueryRepository {

    private final JPAQueryFactory queryFactory;
    private final QLiveBroadcast liveBroadcast = QLiveBroadcast.liveBroadcast;

    @Override
    public Page<LiveBroadcastResponseDto> searchByBroadcastName(String keyword, Pageable pageable) {
        BooleanExpression condition = StringUtils.hasText(keyword)
                ? liveBroadcast.broadcastName.containsIgnoreCase(keyword)
                : null;

        BooleanExpression notDeleted = liveBroadcast.deletedStatus.eq(false);

        List<LiveBroadcastResponseDto> content = queryFactory
                .select(Projections.constructor(
                        LiveBroadcastResponseDto.class,
                        liveBroadcast.liveBroadcastId,
                        liveBroadcast.broadcastName,
                        liveBroadcast.startTime,
                        liveBroadcast.endTime,
                        liveBroadcast.broadcastStatus,
                        liveBroadcast.totalViewerCount,
                        liveBroadcast.hostId,
                        liveBroadcast.companyId
                ))
                .from(liveBroadcast)
                .where(notDeleted, condition)
                .orderBy(liveBroadcast.createdAt.desc())
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch();

        Long total = Optional.ofNullable(
                queryFactory
                        .select(liveBroadcast.count())
                        .from(liveBroadcast)
                        .where(notDeleted, condition)
                        .fetchOne()
        ).orElse(0L);

        return new PageImpl<>(content, pageable, total);
    }


}
