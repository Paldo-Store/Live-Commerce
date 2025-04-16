package com.live_commerce.product.product.infrastructure.security;

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
            String userId = authentication.getName(); // userId 가져오기
            String role = authentication.getAuthorities().stream()
                    .findFirst()
                    .map(GrantedAuthority::getAuthority)
                    .orElse("ROLE_USER"); // 기본값 ROLE_USER

            System.out.println("🚀 FeignClientInterceptor 적용됨 - Authorization 헤더 추가: " + token);
            requestTemplate.header("Authorization", "Bearer " + token);
            requestTemplate.header("X-Hub-User", userId);
            requestTemplate.header("X-Hub-Role", role);
        } else {
            System.out.println("🚨 FeignClientInterceptor 적용 실패 - SecurityContext에서 토큰을 찾을 수 없음");
        }
    }

}
