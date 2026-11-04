package com.taskly.backend.tasks.application;

import com.taskly.backend.shared.domain.EntityIds;
import com.taskly.backend.shared.domain.ForbiddenException;
import com.taskly.backend.shared.domain.NotFoundException;
import com.taskly.backend.tasks.domain.Task;
import com.taskly.backend.tasks.domain.TaskRepository;
import java.util.UUID;
import org.springframework.stereotype.Service;

@Service
public class DeleteTask {

	private final TaskRepository taskRepository;

	public DeleteTask(TaskRepository taskRepository) {
		this.taskRepository = taskRepository;
	}

	public void execute(UUID requesterId, String taskId) {
		UUID id = EntityIds.parse(taskId, "task");
		Task task = taskRepository.findById(id).orElseThrow(() -> new NotFoundException("Task not found!"));
		if (!task.isOwnedBy(requesterId)) {
			throw new ForbiddenException("You are not authorized to delete this task");
		}
		if (!taskRepository.deleteById(id)) {
			throw new NotFoundException("Task not found!");
		}
	}
}
