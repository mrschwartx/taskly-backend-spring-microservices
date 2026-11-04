package com.taskly.backend.integration;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT, properties = {
		"logging.file.name=",
		"taskly.auth.secret=integration-test-secret-integration-test-secret",
		"taskly.auth.bcrypt-rounds=4" })
abstract class IntegrationTestSupport {

	static final Path PUBLIC_DIR = createPublicDir();

	@Value("${local.server.port}")
	private int port;

	@DynamicPropertySource
	static void configure(DynamicPropertyRegistry registry) {
		TestDatabase database = TestDatabase.INSTANCE;
		registry.add("spring.datasource.url", () -> database.jdbcUrl);
		registry.add("spring.datasource.username", () -> database.username);
		registry.add("spring.datasource.password", () -> database.password);
		registry.add("taskly.storage.public-dir", PUBLIC_DIR::toString);
	}

	ApiClient client() {
		return new ApiClient("http://127.0.0.1:" + port);
	}

	record Registered(ApiClient client, String id, String username, String email, String password) {
	}

	Registered register() {
		ApiClient client = client();
		String suffix = UUID.randomUUID().toString().substring(0, 8);
		String username = "user_" + suffix;
		String email = suffix + "@mail.com";
		String password = "Password123!";
		ApiResponse response = client.post("/api/v1/auth/signup",
				ApiClient.json("username", username, "email", email, "password", password));
		if (response.status() != 200) {
			throw new IllegalStateException("Signup failed: " + response.body());
		}
		return new Registered(client, response.json("$._id"), username, email, password);
	}

	String taskJson(String name, String status) {
		return ApiClient.json("name", name, "description", "Finish reading", "priority", "low", "due",
				"2026-12-08T10:00:00Z", "status", status);
	}

	long avatarFileCount() {
		try (var files = Files.list(PUBLIC_DIR.resolve("images").resolve("users"))) {
			return files.filter(file -> file.getFileName().toString().startsWith("avatar-")).count();
		}
		catch (java.nio.file.NoSuchFileException error) {
			return 0;
		}
		catch (IOException error) {
			throw new UncheckedIOException(error);
		}
	}

	private static Path createPublicDir() {
		try {
			return Files.createTempDirectory("taskly-public");
		}
		catch (IOException error) {
			throw new UncheckedIOException(error);
		}
	}
}
