package com.taskly.user.support;

import com.taskly.user.shared.application.EventBus;
import com.taskly.user.shared.domain.DomainEvent;
import java.util.ArrayList;
import java.util.List;

public class RecordingEventBus implements EventBus {

	public final List<DomainEvent> published = new ArrayList<>();

	@Override
	public void publish(DomainEvent event) {
		published.add(event);
	}
}
