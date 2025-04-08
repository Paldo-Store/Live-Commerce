package com.live_commerce.livebroadcast.infrastructure.config;

import com.live_commerce.livebroadcast.infrastructure.security.AuthenticationFilter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;



/**
 *  Spring Security를 적용하여 jwt 기반 인증을 활성화하는 역할
 *  모든 요청이 들어올때 JwtAuthenticationFilter를 적용하여 Jwt가 유효한지 검증
 */
@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {

    private final AuthenticationFilter authenticationFilter;

    public SecurityConfig(AuthenticationFilter authenticationFilter) {
        this.authenticationFilter = authenticationFilter;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                // CSRF 보호를 비활성화합니다. CSRF 보호는 주로 브라우저 클라이언트를 대상으로 하는 공격을 방지하기 위해 사용됩니다.
                .csrf(csrf -> csrf.disable())
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(
                                "/docs", // 추가
                                "/api-docs-livebroadcast-service",
                                "/api-docs-livebroadcast-service/**", // 추가
                                "/livebroadcast/v3/api-docs", // 변경
                                "/livebroadcast/v3/api-docs/**", // 변경
                                "/v3/api-docs/**",
                                "/swagger-ui/**",
                                "/swagger-ui.html",
                                "/swagger-resources/**",
                                "/swagger-resources",
                                "/swagger-resources/configuration/ui",
                                "/swagger-resources/configuration/security",
                                "/webjars/**",
                                "/actuator/**",
                                "/api/v1/auth/**"
                        ).permitAll()
                        .anyRequest().authenticated()
                )
                // 세션 관리 정책을 정의합니다. 여기서는 세션을 사용하지 않도록 STATELESS로 설정합니다. 지금은 jwt 사용할 것이기 때문.
                .sessionManagement(session -> session
                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                )
                // JWT 필터 추가
                .addFilterBefore(authenticationFilter, UsernamePasswordAuthenticationFilter.class);

        // 설정된 보안 필터 체인을 반환합니다.
        return http.build();
    }
}
