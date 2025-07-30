package com.live_commerce.user.application.dto.auth.response;

import com.live_commerce.user.domain.model.User;
import com.live_commerce.user.domain.model.UserRole;

public record UserSignUpResponseDto(
	String username,
	String nickname,
	String email,
	boolean alarmConsent,
	UserRole userRole
) {
	public static UserSignUpResponseDto from(User user) {
		return new UserSignUpResponseDto(
			user.getUsername(),
			user.getNickname(),
			user.getEmail(),
			user.isAlarmConsent(),
			user.getUserRole()
		);
	}
}
