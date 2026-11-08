package com.taskly.gateway.config;

import static org.springframework.cloud.gateway.support.RouteMetadataUtils.CONNECT_TIMEOUT_ATTR;
import static org.springframework.cloud.gateway.support.RouteMetadataUtils.RESPONSE_TIMEOUT_ATTR;

import org.springframework.cloud.gateway.filter.ratelimit.KeyResolver;
import org.springframework.cloud.gateway.filter.ratelimit.RedisRateLimiter;
import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.GatewayFilterSpec;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;

@Configuration
public class RoutesConfig {

	static final String USER_SERVICE = "user-service";

	static final String TASK_SERVICE = "task-service";

	@Bean
	RouteLocator routeLocator(RouteLocatorBuilder builder, GatewayProperties properties,
			RedisRateLimiter authRateLimiter, KeyResolver clientAddressKeyResolver) {
		int connectTimeout = Math.toIntExact(properties.timeouts().connect().toMillis());
		int responseTimeout = Math.toIntExact(properties.timeouts().response().toMillis());
		int uploadTimeout = Math.toIntExact(properties.timeouts().upload().toMillis());
		GatewayProperties.Retry retry = properties.retry();

		return builder.routes()
				.route("auth-credentials", route -> route.order(0)
						.path("/api/v1/auth/signin", "/api/v1/auth/signup")
						.filters(filters -> resilient(filters, USER_SERVICE, retry)
								.requestRateLimiter(config -> {
									config.setRateLimiter(authRateLimiter);
									config.setKeyResolver(clientAddressKeyResolver);
								}))
						.uri("lb://" + USER_SERVICE)
						.metadata(CONNECT_TIMEOUT_ATTR, connectTimeout)
						.metadata(RESPONSE_TIMEOUT_ATTR, responseTimeout))
				.route("auth", route -> route.order(1)
						.path("/api/v1/auth/**")
						.filters(filters -> resilient(filters, USER_SERVICE, retry))
						.uri("lb://" + USER_SERVICE)
						.metadata(CONNECT_TIMEOUT_ATTR, connectTimeout)
						.metadata(RESPONSE_TIMEOUT_ATTR, responseTimeout))
				.route("users-upload", route -> route.order(2)
						.path("/api/v1/users/upload/**")
						.filters(filters -> resilient(filters, USER_SERVICE, retry))
						.uri("lb://" + USER_SERVICE)
						.metadata(CONNECT_TIMEOUT_ATTR, connectTimeout)
						.metadata(RESPONSE_TIMEOUT_ATTR, uploadTimeout))
				.route("users", route -> route.order(3)
						.path("/api/v1/users", "/api/v1/users/**")
						.filters(filters -> resilient(filters, USER_SERVICE, retry))
						.uri("lb://" + USER_SERVICE)
						.metadata(CONNECT_TIMEOUT_ATTR, connectTimeout)
						.metadata(RESPONSE_TIMEOUT_ATTR, responseTimeout))
				.route("tasks", route -> route.order(4)
						.path("/api/v1/tasks", "/api/v1/tasks/**")
						.filters(filters -> resilient(filters, TASK_SERVICE, retry))
						.uri("lb://" + TASK_SERVICE)
						.metadata(CONNECT_TIMEOUT_ATTR, connectTimeout)
						.metadata(RESPONSE_TIMEOUT_ATTR, responseTimeout))
				.route("avatars", route -> route.order(5)
						.path("/images/**")
						.filters(filters -> resilient(filters, USER_SERVICE, retry))
						.uri("lb://" + USER_SERVICE)
						.metadata(CONNECT_TIMEOUT_ATTR, connectTimeout)
						.metadata(RESPONSE_TIMEOUT_ATTR, responseTimeout))
				.build();
	}

	private static GatewayFilterSpec resilient(GatewayFilterSpec filters, String service,
			GatewayProperties.Retry retry) {
		return filters
				.circuitBreaker(config -> {
					config.setName(service);
					config.setFallbackUri("forward:/fallback/" + service);
				})
				.retry(config -> {
					config.setRetries(retry.retries());
					config.setMethods(HttpMethod.GET);
					config.setStatuses(HttpStatus.BAD_GATEWAY, HttpStatus.SERVICE_UNAVAILABLE,
							HttpStatus.GATEWAY_TIMEOUT);
					config.setBackoff(retry.firstBackoff(), retry.maxBackoff(), 2, true);
				});
	}
}
