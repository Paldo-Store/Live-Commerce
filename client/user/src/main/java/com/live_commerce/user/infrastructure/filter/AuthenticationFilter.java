package com.live_commerce.user.infrastructure.filter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

import com.live_commerce.user.infrastructure.security.RequestUserDetails;

@Slf4j
@Component
public class AuthenticationFilter extends OncePerRequestFilter {

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
		String username = request.getHeader("X-User-Username");
		String role = request.getHeader("X-User-Role");

		// 인증 정보가 없다면 401 Unauthorized 처리
		if (username == null || role == null) {
			response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
			return;
		}

		if (!role.startsWith("ROLE_")) {
			role = "ROLE_" + role;
		}

		// 권한 설정
		List<GrantedAuthority> authorities = List.of(new SimpleGrantedAuthority(role));
		UserDetails userDetails = new RequestUserDetails(username, authorities);

		// 인증 정보 설정
		UsernamePasswordAuthenticationToken authentication =
			new UsernamePasswordAuthenticationToken(userDetails, null, authorities);
		SecurityContextHolder.getContext().setAuthentication(authentication);

		log.info("✅ 유저 인증 정보:");
		log.info("   - username: {}", userDetails.getUsername());
		log.info("   - authorities: {}", userDetails.getAuthorities());

		// 필터 체인으로 넘김
		filterChain.doFilter(request, response);
	}
}
