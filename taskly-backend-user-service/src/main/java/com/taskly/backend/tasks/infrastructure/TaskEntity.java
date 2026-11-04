package com.taskly.backend.tasks.infrastructure;

import com.taskly.backend.tasks.domain.DueDate;
import com.taskly.backend.tasks.domain.Task;
import com.taskly.backend.tasks.domain.TaskDescription;
import com.taskly.backend.tasks.domain.TaskName;
import com.taskly.backend.tasks.domain.TaskPriority;
import com.taskly.backend.tasks.domain.TaskStatus;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "tasks")
public class TaskEntity {

	@Id
	@Column(name = "id", nullable = false, updatable = false)
	private UUID id;

	@Column(name = "owner_id", nullable = false, updatable = false)
	private UUID ownerId;

	@Column(name = "name", nullable = false, length = 120)
	private String name;

	@Column(name = "description", nullable = false, length = 2000)
	private String description;

	@Column(name = "priority", nullable = false, length = 50)
	private String priority;

	@Column(name = "due", nullable = false)
	private Instant due;

	@Column(name = "status", nullable = false, length = 50)
	private String status;

	@Column(name = "created_at", nullable = false, updatable = false)
	private Instant createdAt;

	@Column(name = "updated_at", nullable = false)
	private Instant updatedAt;

	protected TaskEntity() {
	}

	static TaskEntity from(Task task) {
		TaskEntity entity = new TaskEntity();
		entity.id = task.getId();
		entity.ownerId = task.getOwnerId();
		entity.createdAt = task.getCreatedAt();
		entity.apply(task);
		return entity;
	}

	void apply(Task task) {
		this.name = task.getName().value();
		this.description = task.getDescription().value();
		this.priority = task.getPriority().value();
		this.due = task.getDue().value();
		this.status = task.getStatus().value();
		this.updatedAt = task.getUpdatedAt();
	}

	Task toDomain() {
		return Task.restore(id, ownerId, TaskName.restore(name), TaskDescription.restore(description),
				TaskPriority.restore(priority), DueDate.restore(due), TaskStatus.restore(status), createdAt,
				updatedAt);
	}
}
