package com.taskly.gateway.config;

import io.github.resilience4j.circuitbreaker.CircuitBreakerConfig;
import io.github.resilience4j.timelimiter.TimeLimiterConfig;
import org.springframework.cloud.circuitbreaker.resilience4j.ReactiveResilience4JCircuitBreakerFactory;
import org.springframework.cloud.circuitbreaker.resilience4j.Resilience4JConfigBuilder;
import org.springframework.cloud.client.circuitbreaker.Customizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ResilienceConfig {

	@Bean
	Customizer<ReactiveResilience4JCircuitBreakerFactory> circuitBreakerCustomizer(GatewayProperties properties) {
		GatewayProperties.CircuitBreaker settings = properties.circuitBreaker();
		return factory -> factory.configureDefault(id -> new Resilience4JConfigBuilder(id)
				.circuitBreakerConfig(CircuitBreakerConfig.custom()
						.slidingWindowType(CircuitBreakerConfig.SlidingWindowType.COUNT_BASED)
						.slidingWindowSize(settings.slidingWindowSize())
						.minimumNumberOfCalls(settings.minimumNumberOfCalls())
						.failureRateThreshold(settings.failureRateThreshold())
						.waitDurationInOpenState(settings.waitDurationInOpenState())
						.permittedNumberOfCallsInHalfOpenState(settings.permittedCallsInHalfOpenState())
						.automaticTransitionFromOpenToHalfOpenEnabled(true)
						.build())
				.timeLimiterConfig(TimeLimiterConfig.custom()
						.timeoutDuration(settings.timeLimit())
						.build())
				.build());
	}
}
