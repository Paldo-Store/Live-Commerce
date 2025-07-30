package com.live_commerce.user.infrastructure.common;

import java.security.SecureRandom;

public class PasswordGenerator {

	private static final char[] CHAR_SET = {
		'0', '1', '2', '3', '4', '5', '6', '7', '8', '9',
		'!', '@', '#', '$', '%', '^', '&'
	};

	public static String generateTempPassword(int length) {
		SecureRandom sr = new SecureRandom();
		StringBuilder sb = new StringBuilder();

		for (int i = 0; i < length; i++) {
			int idx = sr.nextInt(CHAR_SET.length);
			sb.append(CHAR_SET[idx]);
		}

		return sb.toString();
	}
}