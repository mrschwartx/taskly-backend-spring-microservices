package com.taskly.task.infrastructure.messaging;

import org.springframework.stereotype.Component;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.json.JsonMapper;

@Component
public class EventEnvelopeCodec {

	private final JsonMapper mapper = JsonMapper.builder().build();

	public EventEnvelope fromJson(String json) {
		if (json == null || json.isBlank()) {
			throw new InvalidEventException("Empty event payload");
		}
		EventEnvelope envelope;
		try {
			envelope = mapper.readValue(json, EventEnvelope.class);
		}
		catch (JacksonException error) {
			throw new InvalidEventException("Malformed event payload", error);
		}
		if (envelope == null || envelope.name() == null) {
			throw new InvalidEventException("Event has no name");
		}
		return envelope;
	}
}
