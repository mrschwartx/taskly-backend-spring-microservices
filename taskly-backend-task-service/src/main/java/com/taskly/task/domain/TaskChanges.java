package com.taskly.task.domain;

public record TaskChanges(
		TaskName name,
		TaskDescription description,
		TaskPriority priority,
		DueDate due,
		TaskStatus status) {

	public boolean isEmpty() {
		return name == null && description == null && priority == null && due == null && status == null;
	}
}
