package com.taskly.user.application;

import com.taskly.user.shared.domain.Timestamps;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class OutboxRelayService {

	private static final Logger log = LoggerFactory.getLogger(OutboxRelayService.class);

	private static final long MAX_BACKOFF_SECONDS = 60;

	private static final int MAX_BACKOFF_EXPONENT = 6;

	private final OutboxStore store;

	private final MessageSender sender;

	public OutboxRelayService(OutboxStore store, MessageSender sender) {
		this.store = store;
		this.sender = sender;
	}

	public int relay(int batchSize) {
		Instant now = Timestamps.now();
		List<OutboxMessage> pending = store.lockPending(now, batchSize);
		int published = 0;
		for (OutboxMessage message : pending) {
			try {
				sender.send(message);
			}
			catch (RuntimeException error) {
				int attempts = message.attempts() + 1;
				Duration delay = backoff(attempts);
				store.markFailed(message.id(), attempts, now.plus(delay));
				log.warn("Outbox event {} failed attempt={} retryIn={}s: {}", message.id(), attempts,
						delay.toSeconds(), error.getMessage());
				return published;
			}
			store.markPublished(message.id(), Timestamps.now());
			published++;
		}
		return published;
	}

	static Duration backoff(int attempts) {
		long seconds = 1L << Math.min(Math.max(attempts, 1), MAX_BACKOFF_EXPONENT);
		return Duration.ofSeconds(Math.min(seconds, MAX_BACKOFF_SECONDS));
	}
}
