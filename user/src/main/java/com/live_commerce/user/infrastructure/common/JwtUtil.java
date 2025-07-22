package com.live_commerce.user.infrastructure.common;

import java.util.Base64;
import java.util.Date;
import java.util.UUID;

import javax.crypto.SecretKey;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.live_commerce.user.application.exception.CustomException;
import com.live_commerce.user.application.exception.UserExceptionCode;
import com.live_commerce.user.domain.model.UserRole;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.UnsupportedJwtException;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;

@Component
public class JwtUtil {

	public static final String BEARER_PREFIX = "Bearer ";

	@Value("${service.jwt.access-expiration}")
	private long ACCESS_TOKEN_TIME;

	@Value("${service.jwt.refresh-expiration}")
	private long REFRESH_TOKEN_TIME;

	@Value("${service.jwt.secret-key}")
	private String secretKeyString;

	private SecretKey secretKey;

	@PostConstruct
	public void init() {
		byte[] keyBytes = Base64.getDecoder().decode(secretKeyString);
		this.secretKey = Keys.hmacShaKeyFor(keyBytes);
	}

	public String createAccessToken(UUID userId, String username, UserRole role) {
		Date now = new Date();

		return BEARER_PREFIX + Jwts.builder()
			.claim("userId", userId.toString())
			.claim("username", username)
			.claim("role", role.name())
			.issuedAt(now)
			.expiration(new Date(now.getTime() + ACCESS_TOKEN_TIME))
			.signWith(secretKey)
			.compact();
	}

	public String createRefreshToken(UUID userId) {
		Date now = new Date();

		return Jwts.builder()
			.claim("userId", userId.toString())
			.issuedAt(now)
			.expiration(new Date(now.getTime() + REFRESH_TOKEN_TIME))
			.signWith(secretKey)
			.compact();
	}

	public Claims parseClaims(String token) {
		try {
			return Jwts.parser()
				.verifyWith(secretKey)
				.build()
				.parseSignedClaims(removePrefix(token))
				.getPayload();
		} catch (ExpiredJwtException e) {
			throw new CustomException(UserExceptionCode.EXPIRED_TOKEN);
		} catch (SecurityException | MalformedJwtException | UnsupportedJwtException e) {
			throw new CustomException(UserExceptionCode.INVALID_TOKEN);
		} catch (IllegalArgumentException e) {
			throw new CustomException(UserExceptionCode.UNSUPPORTED_TOKEN);
		}
	}

	public void validateToken(String token) {
		try {
			Jwts.parser()
				.verifyWith(secretKey)
				.build()
				.parseSignedClaims(removePrefix(token));
		} catch (JwtException e) {
			throw new CustomException(UserExceptionCode.INVALID_TOKEN);
		}
	}

	private String removePrefix(String token) {
		if (token != null && token.startsWith(BEARER_PREFIX)) {
			return token.substring(BEARER_PREFIX.length());
		}
		return token;
	}
}
