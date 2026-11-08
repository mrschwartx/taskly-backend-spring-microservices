package com.taskly.user.integration;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class UserServiceApplicationIT extends IntegrationTestSupport {

	@Test
	void greetsOnTheRootPath() {
		ApiResponse response = client().get("/");

		assertThat(response.status()).isEqualTo(200);
		assertThat((String) response.json("$.message")).isEqualTo("Welcome to the Taskly API");
	}

	@Test
	void respondsWith404ForUnknownRoutes() {
		ApiResponse response = client().get("/api/v1/unknown");

		assertThat(response.status()).isEqualTo(404);
		assertThat((String) response.json("$.message")).isEqualTo("Not found");
	}

	@Test
	void respondsWith400ForMalformedJson() {
		ApiResponse response = client().rawPost("/api/v1/auth/signin", "{broken", "application/json");

		assertThat(response.status()).isEqualTo(400);
		assertThat((String) response.json("$.message")).isEqualTo("Malformed JSON body");
	}

	@Test
	void echoesAValidCorrelationIdAndGeneratesOneWhenMissing() {
		ApiResponse supplied = client().get("/", java.util.Map.of("X-Correlation-Id", "it-correlation-0001"));
		ApiResponse generated = client().get("/");
		ApiResponse invalid = client().get("/", java.util.Map.of("X-Correlation-Id", "bad id"));

		assertThat(supplied.header("x-correlation-id")).isEqualTo("it-correlation-0001");
		assertThat(generated.header("x-correlation-id")).matches("^[0-9a-f]{8}(-[0-9a-f]{4}){3}-[0-9a-f]{12}$");
		assertThat(invalid.header("x-correlation-id")).isNotEqualTo("bad id");
	}

	@Test
	void exposesTheHealthEndpoint() {
		assertThat(client().get("/actuator/health").status()).isEqualTo(200);
	}
}
