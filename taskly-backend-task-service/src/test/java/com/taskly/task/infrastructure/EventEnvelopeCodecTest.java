package com.taskly.task.infrastructure;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.taskly.task.infrastructure.messaging.EventEnvelope;
import com.taskly.task.infrastructure.messaging.EventEnvelopeCodec;
import com.taskly.task.infrastructure.messaging.InvalidEventException;
import org.junit.jupiter.api.Test;

class EventEnvelopeCodecTest {

	private final EventEnvelopeCodec codec = new EventEnvelopeCodec();

	@Test
	void readsTheUserDeletedContract() {
		EventEnvelope envelope = codec.fromJson("{\"eventId\":\"e1\",\"name\":\"user.deleted\","
				+ "\"occurredAt\":\"2026-01-01T00:00:00.000Z\",\"correlationId\":\"corr-12345678\","
				+ "\"payload\":{\"userId\":\"u1\"}}");

		assertThat(envelope.eventId()).isEqualTo("e1");
		assertThat(envelope.name()).isEqualTo("user.deleted");
		assertThat(envelope.correlationId()).isEqualTo("corr-12345678");
		assertThat(envelope.payload()).containsEntry("userId", "u1");
	}

	@Test
	void ignoresUnknownFieldsForForwardCompatibility() {
		EventEnvelope envelope = codec.fromJson("{\"name\":\"user.deleted\",\"version\":2,\"payload\":{}}");

		assertThat(envelope.name()).isEqualTo("user.deleted");
	}

	@Test
	void rejectsMalformedEmptyAndNamelessPayloads() {
		assertThatThrownBy(() -> codec.fromJson("not json")).isInstanceOf(InvalidEventException.class);
		assertThatThrownBy(() -> codec.fromJson("")).isInstanceOf(InvalidEventException.class);
		assertThatThrownBy(() -> codec.fromJson(null)).isInstanceOf(InvalidEventException.class);
		assertThatThrownBy(() -> codec.fromJson("{\"payload\":{}}")).isInstanceOf(InvalidEventException.class);
	}
}
