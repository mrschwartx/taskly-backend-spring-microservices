package com.taskly.backend.shared.application;

import com.taskly.backend.shared.domain.DomainEvent;
import java.util.function.Consumer;

public interface EventBus {

	void publish(DomainEvent event);

	void subscribe(String eventName, Consumer<DomainEvent> handler);
}
