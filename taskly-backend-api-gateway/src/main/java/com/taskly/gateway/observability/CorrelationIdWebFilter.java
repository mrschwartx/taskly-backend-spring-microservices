package com.taskly.gateway.observability;

import java.time.Duration;
import java.util.UUID;
import java.util.regex.Pattern;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.gateway.route.Route;
import org.springframework.cloud.gateway.support.ServerWebExchangeUtils;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Mono;

@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
public class CorrelationIdWebFilter implements WebFilter {

	public static final String HEADER = "X-Correlation-Id";

	private static final Logger log = LoggerFactory.getLogger(CorrelationIdWebFilter.class);

	private static final Pattern VALID = Pattern.compile("^[A-Za-z0-9._-]{8,100}$");

	@Override
	public Mono<Void> filter(ServerWebExchange exchange, WebFilterChain chain) {
		String incoming = exchange.getRequest().getHeaders().getFirst(HEADER);
		String correlationId = incoming != null && VALID.matcher(incoming).matches() ? incoming
				: UUID.randomUUID().toString();
		ServerHttpRequest request = exchange.getRequest().mutate()
				.headers(headers -> headers.set(HEADER, correlationId))
				.build();
		exchange.getResponse().beforeCommit(() -> {
			exchange.getResponse().getHeaders().set(HEADER, correlationId);
			return Mono.empty();
		});
		ServerWebExchange mutated = exchange.mutate().request(request).build();
		long start = System.nanoTime();
		return chain.filter(mutated).doFinally(signal -> {
			Route route = mutated.getAttribute(ServerWebExchangeUtils.GATEWAY_ROUTE_ATTR);
			log.info("{} {} -> {} in {}ms route={} correlationId={}", mutated.getRequest().getMethod(),
					mutated.getRequest().getURI().getPath(), mutated.getResponse().getStatusCode(),
					Duration.ofNanos(System.nanoTime() - start).toMillis(), route == null ? "none" : route.getId(),
					correlationId);
		});
	}
}
