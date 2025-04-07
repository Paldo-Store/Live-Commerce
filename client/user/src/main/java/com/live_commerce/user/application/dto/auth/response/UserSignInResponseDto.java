package com.live_commerce.user.application.dto.auth.response;

public record UserSignInResponseDto(
	String accessToken,
	String refreshToken
) {
	public static UserSignInResponseDto from(String accessToken, String refreshToken) {
		return new UserSignInResponseDto(accessToken, refreshToken);
	}
}
