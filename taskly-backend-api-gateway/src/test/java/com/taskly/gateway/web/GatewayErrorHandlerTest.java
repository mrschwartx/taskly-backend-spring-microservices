package com.taskly.gateway.web;

import static org.assertj.core.api.Assertions.assertThat;

import java.net.ConnectException;
import java.util.concurrent.TimeoutException;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.mock.http.server.reactive.MockServerHttpRequest;
import org.springframework.mock.web.server.MockServerWebExchange;
import org.springframework.web.server.ResponseStatusException;

class GatewayErrorHandlerTest {

	private final GatewayErrorHandler handler = new GatewayErrorHandler();

	private MockServerWebExchange handle(Throwable error) {
		MockServerWebExchange exchange = MockServerWebExchange.from(MockServerHttpRequest.get("/anything"));
		handler.handle(exchange, error).block();
		return exchange;
	}

	private static String body(MockServerWebExchange exchange) {
		return exchange.getResponse().getBodyAsString().block();
	}

	@Test
	void unknownRoutesKeepTheMonolithNotFoundContract() {
		MockServerWebExchange exchange = handle(new ResponseStatusException(HttpStatus.NOT_FOUND));

		assertThat(exchange.getResponse().getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
		assertThat(exchange.getResponse().getHeaders().getContentType()).isEqualTo(MediaType.APPLICATION_JSON);
		assertThat(body(exchange)).isEqualTo("{\"message\":\"Not found\"}");
	}

	@Test
	void unreachableUpstreamsAreReportedAsServiceUnavailable() {
		MockServerWebExchange exchange = handle(new ConnectException("refused"));

		assertThat(exchange.getResponse().getStatusCode()).isEqualTo(HttpStatus.SERVICE_UNAVAILABLE);
		assertThat(body(exchange)).isEqualTo(
				"{\"status\":503,\"message\":\"Service temporarily unavailable. Please try again later.\"}");
	}

	@Test
	void timeoutsAreReportedAsGatewayTimeout() {
		MockServerWebExchange exchange = handle(new TimeoutException("slow"));

		assertThat(exchange.getResponse().getStatusCode()).isEqualTo(HttpStatus.GATEWAY_TIMEOUT);
		assertThat(body(exchange)).isEqualTo(
				"{\"status\":504,\"message\":\"The service took too long to respond. Please try again later.\"}");
	}

	@Test
	void unexpectedErrorsDoNotLeakDetails() {
		MockServerWebExchange exchange = handle(new IllegalStateException("secret detail"));

		assertThat(exchange.getResponse().getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
		assertThat(body(exchange)).doesNotContain("secret detail").contains("\"status\":500");
	}

	@Test
	void otherClientErrorsKeepTheirStatus() {
		MockServerWebExchange exchange = handle(new ResponseStatusException(HttpStatus.METHOD_NOT_ALLOWED));

		assertThat(exchange.getResponse().getStatusCode()).isEqualTo(HttpStatus.METHOD_NOT_ALLOWED);
		assertThat(body(exchange)).contains("\"status\":405");
	}
}
