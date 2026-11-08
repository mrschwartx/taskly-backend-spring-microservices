package com.taskly.user.application;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public interface OutboxStore {

	void append(OutboxMessage message);

	List<OutboxMessage> lockPending(Instant now, int limit);

	void markPublished(UUID id, Instant at);

	void markFailed(UUID id, int attempts, Instant nextAttemptAt);

	int deletePublishedBefore(Instant before);
}
