package com.live_commerce.payment.infrastructure.security;

import feign.RequestInterceptor;
import feign.RequestTemplate;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;

@Configuration
public class FeignClientInterceptor implements RequestInterceptor {

	@Override
	public void apply(RequestTemplate requestTemplate) {
		Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

		if (authentication != null && authentication.getCredentials() instanceof String token) {
			String userId = authentication.getName();
			String role = authentication.getAuthorities().stream()
				.findFirst()
				.map(GrantedAuthority::getAuthority)
				.orElse("ROLE_USER");

			String username = "";
			Object principal = authentication.getPrincipal();

			if (principal instanceof RequestUserDetails userDetails) {
				username = userDetails.getUsername();
			}

			requestTemplate.header("Authorization", "Bearer " + token);
			requestTemplate.header("X-User-Id", userId);
			requestTemplate.header("X-User-Username", username);
			requestTemplate.header("X-User-Role", role);
		}
	}
}
