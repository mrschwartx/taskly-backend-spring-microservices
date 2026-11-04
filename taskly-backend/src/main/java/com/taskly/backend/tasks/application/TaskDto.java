package com.taskly.backend.tasks.application;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.taskly.backend.shared.domain.Timestamps;
import com.taskly.backend.tasks.domain.Task;

public record TaskDto(
		@JsonProperty("_id") String id,
		String name,
		String description,
		String priority,
		String due,
		String status,
		String owner,
		String createdAt,
		String updatedAt) {

	public static TaskDto from(Task task) {
		return new TaskDto(
				task.getId().toString(),
				task.getName().value(),
				task.getDescription().value(),
				task.getPriority().value(),
				Timestamps.format(task.getDue().value()),
				task.getStatus().value(),
				task.getOwnerId().toString(),
				Timestamps.format(task.getCreatedAt()),
				Timestamps.format(task.getUpdatedAt()));
	}
}
