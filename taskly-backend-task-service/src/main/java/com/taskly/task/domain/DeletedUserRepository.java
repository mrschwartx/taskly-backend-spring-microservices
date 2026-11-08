package com.taskly.task.domain;

import java.time.Instant;
import java.util.UUID;

public interface DeletedUserRepository {

	boolean exists(UUID userId);

	void markDeleted(UUID userId, Instant deletedAt);

	int purgeOlderThan(Instant before);
}
