package com.taskly.user.infrastructure.messaging;

import com.taskly.user.application.OutboxMessage;
import com.taskly.user.application.OutboxStore;
import com.taskly.user.shared.application.EventBus;
import com.taskly.user.shared.domain.DomainEvent;
import com.taskly.user.shared.domain.EntityIds;
import com.taskly.user.shared.domain.Timestamps;
import com.taskly.user.shared.observability.CorrelationContext;
import java.util.UUID;
import org.springframework.stereotype.Component;

@Component
public class OutboxEventBus implements EventBus {

	private final OutboxStore store;

	private final EventEnvelopeCodec codec;

	public OutboxEventBus(OutboxStore store, EventEnvelopeCodec codec) {
		this.store = store;
		this.codec = codec;
	}

	@Override
	public void publish(DomainEvent event) {
		UUID id = EntityIds.generate();
		String correlationId = CorrelationContext.current();
		EventEnvelope envelope = new EventEnvelope(id.toString(), event.name(),
				Timestamps.format(event.occurredAt()), correlationId, event.payload());
		store.append(new OutboxMessage(id, event.key(), event.name(), codec.toJson(envelope), correlationId,
				event.occurredAt(), 0));
	}
}
