package com.live_commerce.user.application.dto.auth.request;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class UserSignInRequestDto {
	private String username;
	private String password;
}
