package com.taskly.backend.tasks.domain;

import com.taskly.backend.shared.domain.Timestamps;
import java.time.Instant;
import java.util.UUID;

public class Task {

	private final UUID id;

	private final UUID ownerId;

	private TaskName name;

	private TaskDescription description;

	private TaskPriority priority;

	private DueDate due;

	private TaskStatus status;

	private final Instant createdAt;

	private Instant updatedAt;

	private Task(UUID id, UUID ownerId, TaskName name, TaskDescription description, TaskPriority priority,
			DueDate due, TaskStatus status, Instant createdAt, Instant updatedAt) {
		this.id = id;
		this.ownerId = ownerId;
		this.name = name;
		this.description = description;
		this.priority = priority;
		this.due = due;
		this.status = status;
		this.createdAt = createdAt;
		this.updatedAt = updatedAt;
	}

	public static Task create(UUID id, UUID ownerId, TaskName name, TaskDescription description,
			TaskPriority priority, DueDate due, TaskStatus status) {
		return create(id, ownerId, name, description, priority, due, status, Timestamps.now());
	}

	public static Task create(UUID id, UUID ownerId, TaskName name, TaskDescription description,
			TaskPriority priority, DueDate due, TaskStatus status, Instant now) {
		return new Task(id, ownerId, name, description, priority, due, status, now, now);
	}

	public static Task restore(UUID id, UUID ownerId, TaskName name, TaskDescription description,
			TaskPriority priority, DueDate due, TaskStatus status, Instant createdAt, Instant updatedAt) {
		return new Task(id, ownerId, name, description, priority, due, status, createdAt, updatedAt);
	}

	public UUID getId() {
		return id;
	}

	public UUID getOwnerId() {
		return ownerId;
	}

	public TaskName getName() {
		return name;
	}

	public TaskDescription getDescription() {
		return description;
	}

	public TaskPriority getPriority() {
		return priority;
	}

	public DueDate getDue() {
		return due;
	}

	public TaskStatus getStatus() {
		return status;
	}

	public Instant getCreatedAt() {
		return createdAt;
	}

	public Instant getUpdatedAt() {
		return updatedAt;
	}

	public boolean isOwnedBy(UUID userId) {
		return ownerId.equals(userId);
	}

	public void update(TaskChanges changes) {
		update(changes, Timestamps.now());
	}

	public void update(TaskChanges changes, Instant now) {
		if (changes.name() != null) {
			this.name = changes.name();
		}
		if (changes.description() != null) {
			this.description = changes.description();
		}
		if (changes.priority() != null) {
			this.priority = changes.priority();
		}
		if (changes.due() != null) {
			this.due = changes.due();
		}
		if (changes.status() != null) {
			this.status = changes.status();
		}
		this.updatedAt = now;
	}
}
