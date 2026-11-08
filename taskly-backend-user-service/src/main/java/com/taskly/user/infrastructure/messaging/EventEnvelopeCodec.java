package com.taskly.user.infrastructure.messaging;

import org.springframework.stereotype.Component;
import tools.jackson.databind.json.JsonMapper;

@Component
public class EventEnvelopeCodec {

	private final JsonMapper mapper = JsonMapper.builder().build();

	public String toJson(EventEnvelope envelope) {
		return mapper.writeValueAsString(envelope);
	}
}
