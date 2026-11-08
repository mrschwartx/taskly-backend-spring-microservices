package com.taskly.user.support;

import com.taskly.user.application.OutboxMessage;
import com.taskly.user.application.OutboxStore;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class InMemoryOutboxStore implements OutboxStore {

	public static final class Entry {

		public OutboxMessage message;

		public Instant publishedAt;

		public Instant nextAttemptAt;

		Entry(OutboxMessage message) {
			this.message = message;
			this.nextAttemptAt = message.createdAt();
		}
	}

	public final List<Entry> entries = new ArrayList<>();

	@Override
	public void append(OutboxMessage message) {
		entries.add(new Entry(message));
	}

	@Override
	public List<OutboxMessage> lockPending(Instant now, int limit) {
		return entries.stream()
				.filter(entry -> entry.publishedAt == null && !entry.nextAttemptAt.isAfter(now))
				.limit(limit)
				.map(entry -> entry.message)
				.toList();
	}

	@Override
	public void markPublished(UUID id, Instant at) {
		find(id).publishedAt = at;
	}

	@Override
	public void markFailed(UUID id, int attempts, Instant nextAttemptAt) {
		Entry entry = find(id);
		OutboxMessage current = entry.message;
		entry.message = new OutboxMessage(current.id(), current.key(), current.eventName(), current.payload(),
				current.correlationId(), current.createdAt(), attempts);
		entry.nextAttemptAt = nextAttemptAt;
	}

	@Override
	public int deletePublishedBefore(Instant before) {
		int size = entries.size();
		entries.removeIf(entry -> entry.publishedAt != null && entry.publishedAt.isBefore(before));
		return size - entries.size();
	}

	private Entry find(UUID id) {
		return entries.stream().filter(entry -> entry.message.id().equals(id)).findFirst().orElseThrow();
	}
}
