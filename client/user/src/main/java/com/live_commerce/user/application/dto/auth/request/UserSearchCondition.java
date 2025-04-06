package com.live_commerce.user.application.dto.auth.request;

import com.live_commerce.user.domain.model.UserRole;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UserSearchCondition {
	private String username;
	private String email;
	private String nickname;
	private UserRole userRole;
	private Boolean alarmConsent;
}
