package com.taskly.gateway.web;

import com.taskly.gateway.observability.CorrelationIdWebFilter;
import java.net.ConnectException;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.TimeoutException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.gateway.support.NotFoundException;
import org.springframework.core.annotation.Order;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebExceptionHandler;
import reactor.core.publisher.Mono;

@Component
@Order(-2)
public class GatewayErrorHandler implements WebExceptionHandler {

	static final String DEFAULT_MESSAGE = "We're having technical issues. Please try again later.";

	static final String TIMEOUT_MESSAGE = "The service took too long to respond. Please try again later.";

	private static final Logger log = LoggerFactory.getLogger(GatewayErrorHandler.class);

	@Override
	public Mono<Void> handle(ServerWebExchange exchange, Throwable error) {
		ServerHttpResponse response = exchange.getResponse();
		if (response.isCommitted()) {
			return Mono.error(error);
		}
		HttpStatusCode status = resolveStatus(error);
		String body = body(status, error);
		log.warn("Gateway error {} {} -> {} correlationId={}: {}", exchange.getRequest().getMethod(),
				exchange.getRequest().getURI().getPath(), status.value(),
				exchange.getRequest().getHeaders().getFirst(CorrelationIdWebFilter.HEADER), error.toString());
		response.setStatusCode(status);
		response.getHeaders().setContentType(MediaType.APPLICATION_JSON);
		DataBuffer buffer = response.bufferFactory().wrap(body.getBytes(StandardCharsets.UTF_8));
		return response.writeWith(Mono.just(buffer));
	}

	static HttpStatusCode resolveStatus(Throwable error) {
		if (error instanceof ResponseStatusException statusException) {
			return statusException.getStatusCode();
		}
		if (error instanceof NotFoundException || error instanceof ConnectException) {
			return HttpStatus.SERVICE_UNAVAILABLE;
		}
		if (error instanceof TimeoutException) {
			return HttpStatus.GATEWAY_TIMEOUT;
		}
		return HttpStatus.INTERNAL_SERVER_ERROR;
	}

	static String body(HttpStatusCode status, Throwable error) {
		int code = status.value();
		if (code == HttpStatus.NOT_FOUND.value()) {
			return "{\"message\":\"Not found\"}";
		}
		String message;
		if (code == HttpStatus.SERVICE_UNAVAILABLE.value() || code == HttpStatus.BAD_GATEWAY.value()) {
			message = FallbackController.UNAVAILABLE_MESSAGE;
		}
		else if (code == HttpStatus.GATEWAY_TIMEOUT.value()) {
			message = TIMEOUT_MESSAGE;
		}
		else if (status.is4xxClientError()) {
			HttpStatus resolved = HttpStatus.resolve(code);
			message = resolved == null ? "Bad request" : resolved.getReasonPhrase();
		}
		else {
			message = DEFAULT_MESSAGE;
		}
		return "{\"status\":" + code + ",\"message\":\"" + message.replace("\\", "\\\\").replace("\"", "\\\"") + "\"}";
	}
}
