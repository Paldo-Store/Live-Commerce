package com.live_commerce.user.application.dto.auth.response;

import com.live_commerce.user.domain.model.User;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class UserUpdateResponseDto {
	private String username;
	private String email;
	private String nickname;
	private boolean alarmConsent;
	private String userRole;

	public static UserUpdateResponseDto from(User user) {
		return UserUpdateResponseDto.builder()
			.username(user.getUsername())
			.email(user.getEmail())
			.nickname(user.getNickname())
			.alarmConsent(user.isAlarmConsent())
			.userRole(user.getUserRole().name())
			.build();
	}
}
