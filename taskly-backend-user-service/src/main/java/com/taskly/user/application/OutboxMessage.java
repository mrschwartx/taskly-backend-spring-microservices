package com.taskly.user.application;

import java.time.Instant;
import java.util.UUID;

public record OutboxMessage(
		UUID id,
		String key,
		String eventName,
		String payload,
		String correlationId,
		Instant createdAt,
		int attempts) {
}
