package com.live_commerce.user.infrastructure.common;

import java.security.Key;
import java.util.Base64;
import java.util.Date;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.live_commerce.user.domain.model.UserRole;
import com.live_commerce.user.application.exception.UserExceptionCode;
import com.live_commerce.user.application.exception.CustomException;

import io.jsonwebtoken.*;

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
	private String secretKey;

	private Key key;

	@PostConstruct
	public void init() {
		byte[] keyBytes = Base64.getDecoder().decode(secretKey);
		this.key = Keys.hmacShaKeyFor(keyBytes);
	}

	public String createAccessToken(UUID userId, String username, UserRole role) {
		Date now = new Date();

		return BEARER_PREFIX + Jwts.builder()
			.claim("userId", userId.toString())
			.claim("username", username)
			.claim("role", role.name())
			.setIssuedAt(now)
			.setExpiration(new Date(now.getTime() + ACCESS_TOKEN_TIME))
			.signWith(key, SignatureAlgorithm.HS256)
			.compact();
	}

	public String createRefreshToken(UUID userId) {
		Date now = new Date();

		return Jwts.builder()
			.claim("userId", userId)
			.setIssuedAt(now)
			.setExpiration(new Date(now.getTime() + REFRESH_TOKEN_TIME))
			.signWith(key, SignatureAlgorithm.HS256)
			.compact();
	}

	public Claims parseClaims(String token) {
		try {
			return Jwts.parserBuilder()
				.setSigningKey(key)
				.build()
				.parseClaimsJws(token)
				.getBody();
		} catch (ExpiredJwtException e) {
			throw new CustomException(UserExceptionCode.EXPIRED_TOKEN);
		} catch (SecurityException | MalformedJwtException | SignatureException e) {
			throw new CustomException(UserExceptionCode.INVALID_TOKEN);
		} catch (UnsupportedJwtException | IllegalArgumentException e) {
			throw new CustomException(UserExceptionCode.UNSUPPORTED_TOKEN);
		}
	}

	public void validateToken(String token) {
		try {
			Jwts.parserBuilder().setSigningKey(key).build().parseClaimsJws(token);
		} catch (JwtException e) {
			throw new CustomException(UserExceptionCode.INVALID_TOKEN);
		}
	}
}

