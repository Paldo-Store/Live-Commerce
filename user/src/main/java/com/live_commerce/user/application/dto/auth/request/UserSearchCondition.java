package com.live_commerce.user.application.dto.auth.request;

import com.live_commerce.user.domain.model.UserRole;

public record UserSearchCondition(
	String username,
	String email,
	String nickname,
	UserRole userRole,
	Boolean alarmConsent
) {
}
