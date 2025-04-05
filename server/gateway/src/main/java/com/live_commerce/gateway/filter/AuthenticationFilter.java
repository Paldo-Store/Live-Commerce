package com.live_commerce.gateway.filter;

import com.live_commerce.gateway.util.JwtUtil;
import io.jsonwebtoken.Claims;
import lombok.RequiredArgsConstructor;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

@Component
@RequiredArgsConstructor
public class AuthenticationFilter implements GlobalFilter {

	private final JwtUtil jwtUtil;

	@Override
	public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
		String path = exchange.getRequest().getURI().getPath();

		if (isPublicPath(path)) {
			return chain.filter(exchange);
		}

		String token = jwtUtil.extractToken(exchange);

		if (token == null || !jwtUtil.validateToken(token)) {
			exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
			return exchange.getResponse().setComplete();
		}

		Claims claims = jwtUtil.parseClaims(token);
		String username = claims.get("username", String.class);
		String role = claims.get("role", String.class);

		// 내부 마이크로서비스로 사용자 정보 전달
		ServerWebExchange modifiedExchange = exchange.mutate()
			.request(exchange.getRequest().mutate()
				.header("X-User-Username", username)
				.header("X-User-Role", role)
				.build())
			.build();

		return chain.filter(modifiedExchange);
	}

	private boolean isPublicPath(String path) {
		return path.startsWith("/api/v1/auth/") ||
			path.startsWith("/swagger-ui/") ||
			path.startsWith("/v3/api-docs/") ||
			path.startsWith("/actuator");
	}
}
