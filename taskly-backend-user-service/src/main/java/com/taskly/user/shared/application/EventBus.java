package com.taskly.user.shared.application;

import com.taskly.user.shared.domain.DomainEvent;

public interface EventBus {

	void publish(DomainEvent event);
}
