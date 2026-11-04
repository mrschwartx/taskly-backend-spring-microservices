package com.taskly.backend.shared.domain;

import java.time.Instant;
import java.util.Map;

public record DomainEvent(String name, Map<String, Object> payload, Instant occurredAt) {

	public DomainEvent {
		payload = Map.copyOf(payload);
	}

	public static DomainEvent of(String name, Map<String, Object> payload) {
		return new DomainEvent(name, payload, Timestamps.now());
	}
}
