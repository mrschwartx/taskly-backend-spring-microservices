package com.taskly.user.config;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.nio.file.Path;
import java.time.Duration;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.util.unit.DataSize;
import org.springframework.validation.annotation.Validated;

@Validated
@ConfigurationProperties(prefix = "taskly")
public record TasklyProperties(
		@Valid @NotNull App app,
		@Valid @NotNull Auth auth,
		@Valid @NotNull Storage storage,
		@Valid @NotNull Messaging messaging) {

	public record App(@NotBlank String level) {

		public boolean production() {
			return "production".equals(level);
		}
	}

	public record Auth(
			@NotBlank(message = "AUTH_SECRET is required")
			@Size(min = 32, message = "AUTH_SECRET must be at least 32 characters long") String secret,
			@NotNull Duration tokenTtl,
			@NotBlank String cookieName,
			String cookieDomain,
			@Min(4) @Max(31) int bcryptRounds) {
	}

	public record Storage(@NotBlank String publicDir, @NotNull DataSize avatarMaxSize) {

		public Path publicPath() {
			return Path.of(publicDir).toAbsolutePath().normalize();
		}
	}

	public record Messaging(
			@NotBlank String userEventsTopic,
			@Min(1) int partitions,
			@Min(1) int replicas,
			@NotNull Duration sendTimeout,
			@Valid @NotNull Outbox outbox) {
	}

	public record Outbox(boolean relayEnabled, @Min(1) int batchSize, @NotNull Duration retention) {
	}
}
