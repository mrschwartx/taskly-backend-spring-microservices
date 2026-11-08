package com.taskly.task.infrastructure.messaging;

import com.taskly.task.application.HandleUserDeleted;
import com.taskly.task.shared.domain.IntegrationEvents;
import com.taskly.task.shared.domain.ValidationException;
import com.taskly.task.shared.observability.CorrelationContext;
import java.nio.charset.StandardCharsets;
import java.util.UUID;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.common.header.Header;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
public class UserEventsListener {

	private static final Logger log = LoggerFactory.getLogger(UserEventsListener.class);

	private final HandleUserDeleted handleUserDeleted;

	private final EventEnvelopeCodec codec;

	public UserEventsListener(HandleUserDeleted handleUserDeleted, EventEnvelopeCodec codec) {
		this.handleUserDeleted = handleUserDeleted;
		this.codec = codec;
	}

	@KafkaListener(topics = "${taskly.messaging.user-events-topic}")
	public void onMessage(ConsumerRecord<String, String> record) {
		EventEnvelope envelope = codec.fromJson(record.value());
		String correlationId = resolveCorrelationId(record, envelope);
		try (MDC.MDCCloseable ignored = MDC.putCloseable(CorrelationContext.MDC_KEY, correlationId)) {
			handle(envelope);
		}
	}

	private void handle(EventEnvelope envelope) {
		if (!IntegrationEvents.USER_DELETED.equals(envelope.name())) {
			log.debug("Ignoring event {} of type {}", envelope.eventId(), envelope.name());
			return;
		}
		Object userId = envelope.payload() == null ? null : envelope.payload().get("userId");
		try {
			int removed = handleUserDeleted.execute(userId);
			log.info("Handled {} event={} userId={} removedTasks={}", envelope.name(), envelope.eventId(), userId,
					removed);
		}
		catch (ValidationException error) {
			throw new InvalidEventException("Event " + envelope.eventId() + " has an invalid user id", error);
		}
	}

	private String resolveCorrelationId(ConsumerRecord<String, String> record, EventEnvelope envelope) {
		Header header = record.headers().lastHeader(CorrelationContext.HEADER);
		if (header != null && header.value() != null) {
			String value = new String(header.value(), StandardCharsets.UTF_8);
			if (CorrelationContext.isValid(value)) {
				return value;
			}
		}
		if (CorrelationContext.isValid(envelope.correlationId())) {
			return envelope.correlationId();
		}
		return UUID.randomUUID().toString();
	}
}
