package com.live_commerce.user.application.dto.auth.response;

import com.live_commerce.user.domain.model.User;
import com.live_commerce.user.domain.model.UserRole;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class UserSignUpResponseDto {
	private String username;
	private String nickname;
	private String email;
	private boolean alarmConsent;
	private UserRole userRole;

	public static UserSignUpResponseDto from(User user) {
		return UserSignUpResponseDto.builder()
			.username(user.getUsername())
			.nickname(user.getNickname())
			.email(user.getEmail())
			.alarmConsent(user.isAlarmConsent())
			.userRole(user.getUserRole())
			.build();
	}
}