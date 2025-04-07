package com.live_commerce.user.application.dto.auth.request;

import com.live_commerce.user.domain.model.UserRole;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class UserUpdateRequestDto {
	@Size(min = 8, max = 20, message = "비밀번호는 8~20자여야 합니다.")
	@Pattern(
		regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[@$!%*?&])[A-Za-z\\d@$!%*?&]+$",
		message = "비밀번호는 대문자, 소문자, 숫자, 특수문자를 각각 최소 1개 이상 포함해야 합니다.")
	private String password;

	@Email(message = "올바른 이메일 형식이 아닙니다.")
	private String email;

	private String nickname;

	private Boolean alarmConsent;

	private UserRole userRole;
}