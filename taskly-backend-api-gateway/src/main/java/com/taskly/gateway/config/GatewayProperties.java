package com.taskly.gateway.config;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.Duration;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

@Validated
@ConfigurationProperties(prefix = "taskly.gateway")
public record GatewayProperties(
		@NotBlank String clientUrl,
		@Valid @NotNull Timeouts timeouts,
		@Valid @NotNull Retry retry,
		@Valid @NotNull RateLimit rateLimit,
		@Valid @NotNull CircuitBreaker circuitBreaker) {

	public record Timeouts(@NotNull Duration connect, @NotNull Duration response, @NotNull Duration upload) {
	}

	public record Retry(@Min(0) int retries, @NotNull Duration firstBackoff, @NotNull Duration maxBackoff) {
	}

	public record RateLimit(@Min(1) int authReplenishRate, @Min(1) int authBurstCapacity) {
	}

	public record CircuitBreaker(
			@Min(1) int slidingWindowSize,
			@Min(1) int minimumNumberOfCalls,
			@Min(1) @Max(100) int failureRateThreshold,
			@NotNull Duration waitDurationInOpenState,
			@Min(1) int permittedCallsInHalfOpenState,
			@NotNull Duration timeLimit) {
	}
}
