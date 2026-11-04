package com.taskly.backend.integration;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Map;
import org.junit.jupiter.api.Test;

class TasklyBackendApplicationIT extends IntegrationTestSupport {

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
	void allowsTheConfiguredClientOriginWithCredentials() {
		ApiResponse response = client().options("/api/v1/auth/signin", Map.of(
				"Origin", "http://localhost:3000",
				"Access-Control-Request-Method", "POST",
				"Access-Control-Request-Headers", "content-type"));

		assertThat(response.header("access-control-allow-origin")).isEqualTo("http://localhost:3000");
		assertThat(response.header("access-control-allow-credentials")).isEqualTo("true");
	}

	@Test
	void exposesTheHealthEndpoint() {
		assertThat(client().get("/actuator/health").status()).isEqualTo(200);
	}
}
