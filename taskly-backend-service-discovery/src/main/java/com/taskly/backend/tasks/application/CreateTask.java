package com.taskly.backend.tasks.application;

import com.taskly.backend.tasks.domain.DueDate;
import com.taskly.backend.tasks.domain.Task;
import com.taskly.backend.tasks.domain.TaskDescription;
import com.taskly.backend.tasks.domain.TaskName;
import com.taskly.backend.tasks.domain.TaskPriority;
import com.taskly.backend.tasks.domain.TaskRepository;
import com.taskly.backend.tasks.domain.TaskStatus;
import java.util.Map;
import java.util.UUID;
import org.springframework.stereotype.Service;

@Service
public class CreateTask {

	private final TaskRepository taskRepository;

	public CreateTask(TaskRepository taskRepository) {
		this.taskRepository = taskRepository;
	}

	public TaskDto execute(UUID ownerId, Map<String, Object> input) {
		Task task = Task.create(
				taskRepository.nextId(),
				ownerId,
				TaskName.create(input.get("name")),
				input.containsKey("description") ? TaskDescription.create(input.get("description"))
						: TaskDescription.empty(),
				TaskPriority.create(input.get("priority")),
				DueDate.create(input.get("due")),
				input.containsKey("status") ? TaskStatus.create(input.get("status")) : TaskStatus.defaultStatus());
		taskRepository.add(task);
		return TaskDto.from(task);
	}
}
