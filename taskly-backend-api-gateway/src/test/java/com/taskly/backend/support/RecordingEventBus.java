package com.taskly.backend.support;

import com.taskly.backend.shared.application.EventBus;
import com.taskly.backend.shared.domain.DomainEvent;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public class RecordingEventBus implements EventBus {

	public final List<DomainEvent> published = new ArrayList<>();

	@Override
	public void publish(DomainEvent event) {
		published.add(event);
	}

	@Override
	public void subscribe(String eventName, Consumer<DomainEvent> handler) {
	}
}
