package com.live_commerce.user.application.dto.user.response;

import com.live_commerce.user.domain.model.User;
import com.live_commerce.user.domain.model.UserRole;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class UserGetResponseDto {
	private String username;
	private String nickname;
	private String email;
	private boolean alarmConsent;
	private UserRole userRole;

	public static UserGetResponseDto from(User user) {
		return UserGetResponseDto.builder()
			.username(user.getUsername())
			.nickname(user.getNickname())
			.email(user.getEmail())
			.alarmConsent(user.isAlarmConsent())
			.userRole(user.getUserRole())
			.build();
	}
}
