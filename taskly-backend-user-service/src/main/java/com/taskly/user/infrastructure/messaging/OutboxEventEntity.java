package com.taskly.user.infrastructure.messaging;

import com.taskly.user.application.OutboxMessage;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "outbox_events")
public class OutboxEventEntity {

	@Id
	@Column(name = "id", nullable = false, updatable = false)
	private UUID id;

	@Column(name = "event_key", nullable = false, updatable = false, length = 100)
	private String eventKey;

	@Column(name = "event_name", nullable = false, updatable = false, length = 100)
	private String eventName;

	@Column(name = "payload", nullable = false, updatable = false)
	private String payload;

	@Column(name = "correlation_id", updatable = false, length = 100)
	private String correlationId;

	@Column(name = "created_at", nullable = false, updatable = false)
	private Instant createdAt;

	@Column(name = "published_at")
	private Instant publishedAt;

	@Column(name = "attempts", nullable = false)
	private int attempts;

	@Column(name = "next_attempt_at", nullable = false)
	private Instant nextAttemptAt;

	protected OutboxEventEntity() {
	}

	static OutboxEventEntity from(OutboxMessage message) {
		OutboxEventEntity entity = new OutboxEventEntity();
		entity.id = message.id();
		entity.eventKey = message.key();
		entity.eventName = message.eventName();
		entity.payload = message.payload();
		entity.correlationId = message.correlationId();
		entity.createdAt = message.createdAt();
		entity.attempts = message.attempts();
		entity.nextAttemptAt = message.createdAt();
		return entity;
	}

	OutboxMessage toMessage() {
		return new OutboxMessage(id, eventKey, eventName, payload, correlationId, createdAt, attempts);
	}

	void markPublished(Instant at) {
		this.publishedAt = at;
	}

	void markFailed(int newAttempts, Instant newNextAttemptAt) {
		this.attempts = newAttempts;
		this.nextAttemptAt = newNextAttemptAt;
	}
}
