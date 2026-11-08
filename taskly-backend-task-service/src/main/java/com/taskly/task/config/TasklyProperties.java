package com.taskly.task.config;

import jakarta.validation.Valid;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.time.Duration;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

@Validated
@ConfigurationProperties(prefix = "taskly")
public record TasklyProperties(
		@Valid @NotNull Auth auth,
		@Valid @NotNull Messaging messaging,
		@Valid @NotNull Tombstones tombstones) {

	public record Auth(
			@NotBlank(message = "AUTH_SECRET is required")
			@Size(min = 32, message = "AUTH_SECRET must be at least 32 characters long") String secret,
			@NotBlank String cookieName) {
	}

	public record Messaging(
			@NotBlank String userEventsTopic,
			@Min(1) int partitions,
			@Min(1) int replicas,
			@Valid @NotNull Retry retry) {
	}

	public record Retry(
			@Min(0) int maxRetries,
			@NotNull Duration initialInterval,
			@NotNull Duration maxInterval,
			@DecimalMin("1.0") double multiplier) {
	}

	public record Tombstones(@NotNull Duration retention) {
	}
}
