package com.taskly.gateway.config;

import static org.assertj.core.api.Assertions.assertThat;

import java.net.InetSocketAddress;
import org.junit.jupiter.api.Test;
import org.springframework.mock.http.server.reactive.MockServerHttpRequest;
import org.springframework.mock.web.server.MockServerWebExchange;

class RateLimiterConfigTest {

	@Test
	void keysClientsByRemoteAddress() {
		MockServerWebExchange exchange = MockServerWebExchange.from(MockServerHttpRequest.post("/api/v1/auth/signin")
				.remoteAddress(new InetSocketAddress("10.1.2.3", 51000)));

		assertThat(RateLimiterConfig.clientAddress(exchange)).isEqualTo("10.1.2.3");
	}

	@Test
	void fallsBackToAnUnknownKeyWithoutARemoteAddress() {
		MockServerWebExchange exchange = MockServerWebExchange
				.from(MockServerHttpRequest.post("/api/v1/auth/signin"));

		assertThat(RateLimiterConfig.clientAddress(exchange)).isEqualTo("unknown");
	}
}
