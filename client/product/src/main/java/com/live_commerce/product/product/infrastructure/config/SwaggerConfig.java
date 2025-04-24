package com.live_commerce.product.product.infrastructure.config;

import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.servers.Server;

@Configuration
@OpenAPIDefinition
public class SwaggerConfig {

	@Value("${gateway.base-url}")
	private String gatewayUrl;

	@Bean
	public OpenAPI customOpenAPI() {
		return new OpenAPI()
			.servers(List.of(
				new Server().url(gatewayUrl).description("Gateway Port")
			))
			.components(new Components()
				.addSecuritySchemes("bearerAuth", new SecurityScheme()
					.type(SecurityScheme.Type.HTTP)
					.scheme("bearer")
					.bearerFormat("JWT")
					.in(SecurityScheme.In.HEADER)
					.name("Authorization")
				)
			)
			.addSecurityItem(new SecurityRequirement().addList("bearerAuth"))
			.info(new Info()
				.title("Live Commerce - Product Service API")
				.description("Product 서비스 관련 API 명세서입니다.")
				.version("v1.0"));
	}
}
