package com.live_commerce.user.application.dto.auth.response;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class UserSignInResponseDto {
	private String accessToken;
	private String refreshToken;

	public static UserSignInResponseDto from(String accessToken, String refreshToken) {
		return UserSignInResponseDto.builder()
			.accessToken(accessToken)
			.refreshToken(refreshToken)
			.build();
	}
}
