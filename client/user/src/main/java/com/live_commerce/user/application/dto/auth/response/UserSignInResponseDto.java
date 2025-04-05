package com.live_commerce.user.application.dto.auth.response;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class UserSignInResponseDto {
	private String accessToken;
	private String refreshToken;
}
