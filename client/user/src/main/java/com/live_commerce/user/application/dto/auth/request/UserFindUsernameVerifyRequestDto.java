package com.live_commerce.user.application.dto.auth.request;

import jakarta.validation.constraints.Email;

public record UserFindUsernameVerifyRequestDto(
	@Email(message = "올바른 이메일 형식이 아닙니다.")
	String email,
	String code
) {
}
