package com.live_commerce.payment.infrastructure.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.listener.PatternTopic;
import org.springframework.data.redis.listener.RedisMessageListenerContainer;

import com.live_commerce.payment.infrastructure.listener.PaymentExpirationListener;

@Configuration
public class RedisTTLListenerConfig {

	@Bean
	public RedisMessageListenerContainer redisContainer(
		RedisConnectionFactory connectionFactory,
		PaymentExpirationListener expirationListener
	) {
		RedisMessageListenerContainer container = new RedisMessageListenerContainer();
		container.setConnectionFactory(connectionFactory);
		container.addMessageListener(expirationListener, new PatternTopic("__keyevent@0__:expired"));
		return container;
	}
}

