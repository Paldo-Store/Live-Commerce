package com.live_commerce.user.infrastructure.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import com.live_commerce.user.infrastructure.filter.AuthenticationFilter;

import lombok.RequiredArgsConstructor;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

	private final AuthenticationFilter authenticationFilter;

	@Bean
	public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
		// HTTP 보안 설정
		http
			.csrf((csrf) -> csrf.disable())  // CSRF 비활성화
			.sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS)) // 세션 상태를 사용하지 않음
			.authorizeHttpRequests(auth -> auth
				.requestMatchers(
					"/api/v1/auth/**", // 인증되지 않은 경로
					"/swagger-ui/**",
					"/v3/api-docs/**",
					"/actuator/**"
				).permitAll() // 인증 없이 접근 가능
				.anyRequest().authenticated() // 그 외의 요청은 인증 필요
			)
			.addFilterBefore(authenticationFilter, UsernamePasswordAuthenticationFilter.class); // 인증 필터 추가

		return http.build();
	}
}
