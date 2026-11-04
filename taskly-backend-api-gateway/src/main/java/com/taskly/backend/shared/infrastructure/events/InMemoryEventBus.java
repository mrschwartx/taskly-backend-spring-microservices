package com.taskly.backend.shared.infrastructure.events;

import com.taskly.backend.shared.application.EventBus;
import com.taskly.backend.shared.domain.DomainEvent;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Consumer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class InMemoryEventBus implements EventBus {

	private static final Logger log = LoggerFactory.getLogger(InMemoryEventBus.class);

	private final Map<String, List<Consumer<DomainEvent>>> handlers = new ConcurrentHashMap<>();

	@Override
	public void subscribe(String eventName, Consumer<DomainEvent> handler) {
		handlers.computeIfAbsent(eventName, key -> new CopyOnWriteArrayList<>()).add(handler);
	}

	@Override
	public void publish(DomainEvent event) {
		for (Consumer<DomainEvent> handler : handlers.getOrDefault(event.name(), List.of())) {
			try {
				handler.accept(event);
			}
			catch (RuntimeException error) {
				log.error("Event handler failed for event {}: {}", event.name(), error.getMessage(), error);
			}
		}
	}
}
