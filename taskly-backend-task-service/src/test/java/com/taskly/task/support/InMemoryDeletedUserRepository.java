package com.taskly.task.support;

import com.taskly.task.domain.DeletedUserRepository;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class InMemoryDeletedUserRepository implements DeletedUserRepository {

	public final Map<UUID, Instant> tombstones = new HashMap<>();

	@Override
	public boolean exists(UUID userId) {
		return tombstones.containsKey(userId);
	}

	@Override
	public void markDeleted(UUID userId, Instant deletedAt) {
		tombstones.putIfAbsent(userId, deletedAt);
	}

	@Override
	public int purgeOlderThan(Instant before) {
		int size = tombstones.size();
		tombstones.values().removeIf(deletedAt -> deletedAt.isBefore(before));
		return size - tombstones.size();
	}
}
