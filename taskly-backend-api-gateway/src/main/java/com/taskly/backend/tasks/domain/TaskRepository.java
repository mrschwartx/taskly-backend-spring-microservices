package com.taskly.backend.tasks.domain;

import java.util.Optional;
import java.util.UUID;

public interface TaskRepository {

	UUID nextId();

	void add(Task task);

	boolean update(Task task);

	Optional<Task> findById(UUID id);

	TaskPage findByOwner(UUID ownerId, String status, String orderBy, int page, int pageSize);

	boolean deleteById(UUID id);

	int deleteByOwner(UUID ownerId);
}
