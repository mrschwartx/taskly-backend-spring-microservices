package com.taskly.task.infrastructure.messaging;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.util.Map;

@JsonIgnoreProperties(ignoreUnknown = true)
public record EventEnvelope(
		String eventId,
		String name,
		String occurredAt,
		String correlationId,
		Map<String, Object> payload) {
}
