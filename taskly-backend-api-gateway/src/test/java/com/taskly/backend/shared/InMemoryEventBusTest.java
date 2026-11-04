package com.taskly.backend.shared;

import static org.assertj.core.api.Assertions.assertThat;

import com.taskly.backend.shared.domain.DomainEvent;
import com.taskly.backend.shared.infrastructure.events.InMemoryEventBus;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;

class InMemoryEventBusTest {

	@Test
	void deliversEventsToSubscribersOfTheSameName() {
		InMemoryEventBus bus = new InMemoryEventBus();
		List<String> received = new ArrayList<>();
		bus.subscribe("a", event -> received.add("a:" + event.payload().get("value")));
		bus.subscribe("b", event -> received.add("b"));

		bus.publish(DomainEvent.of("a", Map.of("value", 1)));

		assertThat(received).containsExactly("a:1");
	}

	@Test
	void keepsDeliveringWhenAHandlerFails() {
		InMemoryEventBus bus = new InMemoryEventBus();
		List<String> received = new ArrayList<>();
		bus.subscribe("a", event -> {
			throw new IllegalStateException("boom");
		});
		bus.subscribe("a", event -> received.add("second"));

		bus.publish(DomainEvent.of("a", Map.of()));

		assertThat(received).containsExactly("second");
	}
}
