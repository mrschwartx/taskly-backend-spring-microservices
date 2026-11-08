package com.taskly.user.support;

import com.taskly.user.application.MessagePublishException;
import com.taskly.user.application.MessageSender;
import com.taskly.user.application.OutboxMessage;
import java.util.ArrayList;
import java.util.List;

public class FakeMessageSender implements MessageSender {

	public final List<OutboxMessage> sent = new ArrayList<>();

	public boolean failing;

	@Override
	public void send(OutboxMessage message) {
		if (failing) {
			throw new MessagePublishException("broker unavailable");
		}
		sent.add(message);
	}
}
