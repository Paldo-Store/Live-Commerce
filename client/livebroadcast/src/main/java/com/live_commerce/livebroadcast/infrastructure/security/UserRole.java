package com.live_commerce.livebroadcast.infrastructure.security;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum UserRole {
	CUSTOMER("CUSTOMER"),
	SELLER("SELLER"),
	SHOW_HOST("SHOW_HOST"),
	MASTER("MASTER");

	private final String value;
}

