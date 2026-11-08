package com.taskly.user.application;

public interface MessageSender {

	void send(OutboxMessage message);
}
