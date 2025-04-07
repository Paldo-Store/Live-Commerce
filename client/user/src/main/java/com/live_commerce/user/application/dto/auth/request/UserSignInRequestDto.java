package com.live_commerce.user.application.dto.auth.request;

public record UserSignInRequestDto(
	String username,
	String password
) {
}

