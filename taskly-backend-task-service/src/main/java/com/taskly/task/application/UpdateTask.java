package com.taskly.task.application;

import com.taskly.task.shared.domain.EntityIds;
import com.taskly.task.shared.domain.ForbiddenException;
import com.taskly.task.shared.domain.NotFoundException;
import com.taskly.task.shared.domain.ValidationException;
import com.taskly.task.domain.DueDate;
import com.taskly.task.domain.Task;
import com.taskly.task.domain.TaskChanges;
import com.taskly.task.domain.TaskDescription;
import com.taskly.task.domain.TaskName;
import com.taskly.task.domain.TaskPriority;
import com.taskly.task.domain.TaskRepository;
import com.taskly.task.domain.TaskStatus;
import java.util.Map;
import java.util.UUID;
import org.springframework.stereotype.Service;

@Service
public class UpdateTask {

	private final TaskRepository taskRepository;

	public UpdateTask(TaskRepository taskRepository) {
		this.taskRepository = taskRepository;
	}

	public TaskDto execute(UUID requesterId, String taskId, Map<String, Object> changes) {
		UUID id = EntityIds.parse(taskId, "task");
		Task task = taskRepository.findById(id).orElseThrow(() -> new NotFoundException("Task not found!"));
		if (!task.isOwnedBy(requesterId)) {
			throw new ForbiddenException("You are not authorized to update this task");
		}

		task.update(parseChanges(changes));

		if (!taskRepository.update(task)) {
			throw new NotFoundException("Task not found!");
		}
		return TaskDto.from(task);
	}

	private static TaskChanges parseChanges(Map<String, Object> source) {
		TaskChanges changes = new TaskChanges(
				source.containsKey("name") ? TaskName.create(source.get("name")) : null,
				source.containsKey("description") ? TaskDescription.create(source.get("description")) : null,
				source.containsKey("priority") ? TaskPriority.create(source.get("priority")) : null,
				source.containsKey("due") ? DueDate.create(source.get("due")) : null,
				source.containsKey("status") ? TaskStatus.create(source.get("status")) : null);
		if (changes.isEmpty()) {
			throw new ValidationException("No updatable fields provided");
		}
		return changes;
	}
}
