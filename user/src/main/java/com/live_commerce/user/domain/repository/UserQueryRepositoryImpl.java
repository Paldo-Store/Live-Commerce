package com.live_commerce.user.domain.repository;

import java.util.List;

import com.live_commerce.user.application.dto.auth.request.UserSearchCondition;
import com.live_commerce.user.domain.model.QUser;
import com.live_commerce.user.domain.model.User;
import com.querydsl.core.BooleanBuilder;
import com.querydsl.jpa.impl.JPAQueryFactory;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class UserQueryRepositoryImpl implements UserQueryRepository {

	private final JPAQueryFactory queryFactory;

	@Override
	public List<User> searchUser(UserSearchCondition condition) {
		QUser user = QUser.user;
		BooleanBuilder builder = new BooleanBuilder();

		if (condition.username() != null) {
			builder.and(user.username.eq(condition.username()));
		}
		if (condition.email() != null) {
			builder.and(user.email.eq(condition.email()));
		}
		if (condition.nickname() != null) {
			builder.and(user.nickname.containsIgnoreCase(condition.nickname()));
		}
		if (condition.userRole() != null) {
			builder.and(user.userRole.eq(condition.userRole()));
		}
		if (condition.alarmConsent() != null) {
			builder.and(user.alarmConsent.eq(condition.alarmConsent()));
		}

		return queryFactory
			.selectFrom(user)
			.where(builder)
			.fetch();
	}
}
