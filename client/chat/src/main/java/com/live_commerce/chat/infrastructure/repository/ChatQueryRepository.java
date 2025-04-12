package com.live_commerce.chat.infrastructure.repository;

import com.live_commerce.chat.domain.model.Chat;
import com.live_commerce.chat.domain.model.QChat;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;


@Repository
@RequiredArgsConstructor
public class ChatQueryRepository {

    private final JPAQueryFactory queryFactory;
    private final QChat chat = QChat.chat;

    //Chat 전체 조회
    public Page<Chat> findAll(Pageable pageable) {
        List<Chat> chats = queryFactory
                .selectFrom(chat)
                .where(chat.deletedStatus.eq(false)) // 삭제되지 않은 데이터만 조회
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch();

        long total = queryFactory
                .select(chat.count())
                .from(chat)
                .where(chat.deletedAt.isNull())
                .fetchFirst();

        return new PageImpl<>(chats, pageable, total);
    }

    public Page<Chat> findAllByUserId(UUID userId, Pageable pageable) {
        QChat chat = QChat.chat;

        List<Chat> chats = queryFactory
                .selectFrom(chat)
                .where(chat.userId.eq(userId))
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .orderBy(chat.id.desc())
                .fetch();

        long total = queryFactory
                .select(chat.count())
                .from(chat)
                .where(chat.userId.eq(userId))
                .fetchOne();

        return new PageImpl<>(chats, pageable, total);
    }


}
