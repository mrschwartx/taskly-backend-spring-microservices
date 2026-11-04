package com.taskly.backend.tasks.infrastructure;

import com.taskly.backend.shared.application.EventBus;
import com.taskly.backend.shared.domain.DomainEvent;
import com.taskly.backend.shared.domain.IntegrationEvents;
import com.taskly.backend.tasks.application.DeleteTasksByOwner;
import org.springframework.stereotype.Component;

@Component
public class UserDeletedSubscriber {

	public UserDeletedSubscriber(EventBus eventBus, DeleteTasksByOwner deleteTasksByOwner) {
		eventBus.subscribe(IntegrationEvents.USER_DELETED, (DomainEvent event) -> deleteTasksByOwner
				.execute(String.valueOf(event.payload().get("userId"))));
	}
}
