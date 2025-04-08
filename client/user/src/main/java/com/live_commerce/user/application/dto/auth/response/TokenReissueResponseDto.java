package com.live_commerce.user.application.dto.auth.response;

public record TokenReissueResponseDto(
	String accessToken,
	String refreshToken
) {}
