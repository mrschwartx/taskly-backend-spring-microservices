package com.taskly.backend.tasks.application;

import com.taskly.backend.shared.domain.EntityIds;
import com.taskly.backend.tasks.domain.TaskRepository;
import org.springframework.stereotype.Service;

@Service
public class DeleteTasksByOwner {

	private final TaskRepository taskRepository;

	public DeleteTasksByOwner(TaskRepository taskRepository) {
		this.taskRepository = taskRepository;
	}

	public int execute(Object ownerId) {
		return taskRepository.deleteByOwner(EntityIds.parse(ownerId, "user"));
	}
}
