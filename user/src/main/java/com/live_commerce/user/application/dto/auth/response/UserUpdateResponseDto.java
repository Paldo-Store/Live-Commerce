package com.live_commerce.user.application.dto.auth.response;

import com.live_commerce.user.domain.model.User;
import com.live_commerce.user.domain.model.UserRole;

public record UserUpdateResponseDto(
	String username,
	String email,
	String nickname,
	boolean alarmConsent,
	UserRole userRole
) {
	public static UserUpdateResponseDto from(User user) {
		return new UserUpdateResponseDto(
			user.getUsername(),
			user.getEmail(),
			user.getNickname(),
			user.isAlarmConsent(),
			user.getUserRole()
		);
	}
}
