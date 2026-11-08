package com.taskly.user.infrastructure;

import static org.assertj.core.api.Assertions.assertThat;

import com.taskly.user.application.OutboxMessage;
import com.taskly.user.infrastructure.messaging.EventEnvelopeCodec;
import com.taskly.user.infrastructure.messaging.OutboxEventBus;
import com.taskly.user.shared.domain.DomainEvent;
import com.taskly.user.shared.domain.IntegrationEvents;
import com.taskly.user.shared.observability.CorrelationContext;
import com.taskly.user.support.InMemoryOutboxStore;
import java.util.Map;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.slf4j.MDC;

class OutboxEventBusTest {

	private final InMemoryOutboxStore store = new InMemoryOutboxStore();

	private final OutboxEventBus bus = new OutboxEventBus(store, new EventEnvelopeCodec());

	@AfterEach
	void tearDown() {
		MDC.remove(CorrelationContext.MDC_KEY);
	}

	@Test
	void storesAnEnvelopeWithTheKeyAndTheCorrelationId() {
		MDC.put(CorrelationContext.MDC_KEY, "corr-12345678");

		bus.publish(DomainEvent.of(IntegrationEvents.USER_DELETED, "user-1", Map.of("userId", "user-1")));

		assertThat(store.entries).hasSize(1);
		OutboxMessage message = store.entries.get(0).message;
		assertThat(message.key()).isEqualTo("user-1");
		assertThat(message.eventName()).isEqualTo("user.deleted");
		assertThat(message.correlationId()).isEqualTo("corr-12345678");
		assertThat(message.attempts()).isZero();
		assertThat(message.payload())
				.contains("\"eventId\":\"" + message.id() + "\"")
				.contains("\"name\":\"user.deleted\"")
				.contains("\"correlationId\":\"corr-12345678\"")
				.contains("\"userId\":\"user-1\"");
	}

	@Test
	void storesAnEventWithoutCorrelationIdWhenNoneIsActive() {
		bus.publish(DomainEvent.of(IntegrationEvents.USER_DELETED, "user-2", Map.of("userId", "user-2")));

		assertThat(store.entries.get(0).message.correlationId()).isNull();
	}
}
