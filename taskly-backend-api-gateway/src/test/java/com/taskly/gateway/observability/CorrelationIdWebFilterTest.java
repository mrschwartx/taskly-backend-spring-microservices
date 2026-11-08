package com.taskly.gateway.observability;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.concurrent.atomic.AtomicReference;
import org.junit.jupiter.api.Test;
import org.springframework.mock.http.server.reactive.MockServerHttpRequest;
import org.springframework.mock.web.server.MockServerWebExchange;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Mono;

class CorrelationIdWebFilterTest {

	private static final String UUID_PATTERN = "^[0-9a-f]{8}(-[0-9a-f]{4}){3}-[0-9a-f]{12}$";

	private final CorrelationIdWebFilter filter = new CorrelationIdWebFilter();

	private final AtomicReference<ServerWebExchange> forwarded = new AtomicReference<>();

	private final WebFilterChain chain = exchange -> {
		forwarded.set(exchange);
		return exchange.getResponse().setComplete();
	};

	private MockServerWebExchange run(String incoming) {
		MockServerHttpRequest.BaseBuilder<?> request = MockServerHttpRequest.get("/api/v1/tasks");
		if (incoming != null) {
			request.header(CorrelationIdWebFilter.HEADER, incoming);
		}
		MockServerWebExchange exchange = MockServerWebExchange.from(request);
		Mono<Void> result = filter.filter(exchange, chain);
		result.block();
		return exchange;
	}

	@Test
	void keepsAValidIncomingCorrelationId() {
		MockServerWebExchange exchange = run("client-corr-0001");

		assertThat(forwarded.get().getRequest().getHeaders().getFirst(CorrelationIdWebFilter.HEADER))
				.isEqualTo("client-corr-0001");
		assertThat(exchange.getResponse().getHeaders().get(CorrelationIdWebFilter.HEADER))
				.containsExactly("client-corr-0001");
	}

	@Test
	void generatesACorrelationIdWhenMissing() {
		MockServerWebExchange exchange = run(null);

		String forwardedId = forwarded.get().getRequest().getHeaders().getFirst(CorrelationIdWebFilter.HEADER);
		assertThat(forwardedId).matches(UUID_PATTERN);
		assertThat(exchange.getResponse().getHeaders().getFirst(CorrelationIdWebFilter.HEADER))
				.isEqualTo(forwardedId);
	}

	@Test
	void replacesAnInvalidIncomingCorrelationId() {
		MockServerWebExchange exchange = run("bad id with spaces");

		assertThat(exchange.getResponse().getHeaders().getFirst(CorrelationIdWebFilter.HEADER))
				.matches(UUID_PATTERN);
	}
}
