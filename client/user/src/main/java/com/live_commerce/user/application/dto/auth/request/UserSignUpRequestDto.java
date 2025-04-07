package com.live_commerce.user.application.dto.auth.request;

import com.live_commerce.user.domain.model.User;
import com.live_commerce.user.domain.model.UserRole;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record UserSignUpRequestDto(

	@NotBlank(message = "유저 아이디는 필수 입력값입니다.")
	@Size(min = 4, max = 20, message = "유저 아이디는 4~20자여야 합니다.")
	@Pattern(regexp = "^[a-z0-9]+$", message = "유저 아이디는 소문자와 숫자로만 구성되어야 합니다.")
	String username,

	@NotBlank(message = "비밀번호는 필수 입력값입니다.")
	@Size(min = 8, max = 20, message = "비밀번호는 8~20자여야 합니다.")
	@Pattern(
		regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[@$!%*?&])[A-Za-z\\d@$!%*?&]+$",
		message = "비밀번호는 대문자, 소문자, 숫자, 특수문자를 각각 최소 1개 이상 포함해야 합니다.")
	String password,

	@NotBlank(message = "이메일은 필수 입력값입니다.")
	@Email(message = "올바른 이메일 형식이 아닙니다.")
	String email,

	@NotBlank(message = "닉네임은 필수 입력값입니다.")
	String nickname,

	boolean alarmConsent,

	UserRole userRole

) {
	public User toEntity(String encodedPassword) {
		return User.of(
			this.username,
			encodedPassword,
			this.email,
			this.nickname,
			this.alarmConsent,
			this.userRole
		);
	}
}
