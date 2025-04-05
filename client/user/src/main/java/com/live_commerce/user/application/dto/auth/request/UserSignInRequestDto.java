package com.live_commerce.user.application.dto.auth.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class UserSignInRequestDto {
	private String username;
	private String password;
}
