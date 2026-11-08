package com.taskly.gateway.web;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

class FallbackControllerTest {

	@Test
	void answersWithAServiceUnavailableApiError() {
		ResponseEntity<ApiError> response = new FallbackController().fallback("user-service");

		assertThat(response.getStatusCode()).isEqualTo(HttpStatus.SERVICE_UNAVAILABLE);
		assertThat(response.getHeaders().getFirst("Retry-After")).isEqualTo("5");
		assertThat(response.getBody()).isEqualTo(new ApiError(503, FallbackController.UNAVAILABLE_MESSAGE));
	}

	@Test
	void rootControllerGreetsLikeTheMonolith() {
		assertThat(new RootController().welcome()).containsEntry("message", "Welcome to the Taskly API");
	}
}
