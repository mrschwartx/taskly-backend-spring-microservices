package com.taskly.gateway.config;

import java.net.InetSocketAddress;
import org.springframework.cloud.gateway.filter.ratelimit.KeyResolver;
import org.springframework.cloud.gateway.filter.ratelimit.RedisRateLimiter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

@Configuration
public class RateLimiterConfig {

	private static final String UNKNOWN_CLIENT = "unknown";

	@Bean
	RedisRateLimiter authRateLimiter(GatewayProperties properties) {
		return new RedisRateLimiter(properties.rateLimit().authReplenishRate(),
				properties.rateLimit().authBurstCapacity(), 1);
	}

	@Bean
	KeyResolver clientAddressKeyResolver() {
		return exchange -> Mono.just(clientAddress(exchange));
	}

	static String clientAddress(ServerWebExchange exchange) {
		InetSocketAddress address = exchange.getRequest().getRemoteAddress();
		if (address == null || address.getAddress() == null) {
			return UNKNOWN_CLIENT;
		}
		return address.getAddress().getHostAddress();
	}
}
