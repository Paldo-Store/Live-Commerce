package com.live_commerce.livebroadcast.infrastructure.security;

import com.live_commerce.livebroadcast.application.exception.CustomException;
import io.jsonwebtoken.Claims;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

@Component
public class AuthenticationFilter extends OncePerRequestFilter {

	private final JwtValidator jwtValidator;

	public AuthenticationFilter(JwtValidator jwtValidator) {
		this.jwtValidator = jwtValidator;
	}

	@Override
	protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
		throws ServletException, IOException {

		String requestUri = request.getRequestURI();

		// 인증이 필요 없는 경로는 필터를 통과시킴
		if (requestUri.startsWith("/api/v1/auth/") ||
			requestUri.startsWith("/swagger-ui/") ||
			requestUri.startsWith("/v3/api-docs") ||
			requestUri.startsWith("/actuator")) {

			filterChain.doFilter(request, response);
			return;
		}

		// 요청 헤더에서 사용자 정보 추출
		String authHeader = request.getHeader("Authorization");

		System.out.println("Authorization header: [" + authHeader + "]");

		if (authHeader == null || !authHeader.startsWith("Bearer ")) {
			response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
			return;
		}

		String token = authHeader.substring(7);
		System.out.println("Extracted JWT Token: [" + token + "]");

		try {
			Claims claims = jwtValidator.parseClaims(token); // 유효성 검사 및 파싱

			String username = claims.get("username", String.class);
			String role = claims.get("role", String.class);

			if (username == null || role == null) {
				response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
				return;
			}

			if (!role.startsWith("ROLE_")) {
				role = "ROLE_" + role;
			}

			List<GrantedAuthority> authorities = List.of(new SimpleGrantedAuthority(role));
			UserDetails userDetails = new RequestUserDetails(username, authorities);

			// 인증 정보 설정
			UsernamePasswordAuthenticationToken authentication =
					new UsernamePasswordAuthenticationToken(userDetails, token, authorities);
			SecurityContextHolder.getContext().setAuthentication(authentication);

		} catch (CustomException e) {
			response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
			return;
		}

		// 필터 체인으로 넘김
		filterChain.doFilter(request, response);
	}
}
