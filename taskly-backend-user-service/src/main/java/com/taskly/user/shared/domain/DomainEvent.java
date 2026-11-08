package com.taskly.user.shared.domain;

import java.time.Instant;
import java.util.Map;

public record DomainEvent(String name, String key, Map<String, Object> payload, Instant occurredAt) {

	public DomainEvent {
		payload = Map.copyOf(payload);
	}

	public static DomainEvent of(String name, String key, Map<String, Object> payload) {
		return new DomainEvent(name, key, payload, Timestamps.now());
	}
}
