package com.taskly.task.integration;

import com.taskly.task.support.TestTokens;
import java.time.Duration;
import java.time.Instant;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT, properties = {
		"logging.file.name=",
		"taskly.auth.secret=" + IntegrationTestSupport.SECRET,
		"taskly.messaging.partitions=1",
		"spring.kafka.admin.auto-create=false",
		"spring.kafka.listener.auto-startup=false",
		"eureka.client.enabled=false",
		"spring.cloud.discovery.enabled=false",
		"management.tracing.enabled=false" })
abstract class IntegrationTestSupport {

	static final String SECRET = "integration-test-secret-integration-test-secret";

	@Value("${local.server.port}")
	private int port;

	@DynamicPropertySource
	static void configure(DynamicPropertyRegistry registry) {
		TestDatabase database = TestDatabase.INSTANCE;
		registry.add("spring.datasource.url", () -> database.jdbcUrl);
		registry.add("spring.datasource.username", () -> database.username);
		registry.add("spring.datasource.password", () -> database.password);
	}

	ApiClient client() {
		return new ApiClient("http://127.0.0.1:" + port);
	}

	record Registered(ApiClient client, String id) {
	}

	Registered register() {
		UUID userId = UUID.randomUUID();
		String token = TestTokens.issue(SECRET, userId, Instant.now(), Duration.ofHours(1));
		ApiClient client = client();
		client.setHeader("Cookie", "taskly_token=" + token);
		return new Registered(client, userId.toString());
	}

	String taskJson(String name, String status) {
		return ApiClient.json("name", name, "description", "Finish reading", "priority", "low", "due",
				"2026-12-08T10:00:00Z", "status", status);
	}
}
