package com.taskly.task.application;

import com.taskly.task.domain.DeletedUserRepository;
import com.taskly.task.domain.DueDate;
import com.taskly.task.domain.Task;
import com.taskly.task.domain.TaskDescription;
import com.taskly.task.domain.TaskName;
import com.taskly.task.domain.TaskPriority;
import com.taskly.task.domain.TaskRepository;
import com.taskly.task.domain.TaskStatus;
import com.taskly.task.shared.domain.AuthenticationFailedException;
import java.util.Map;
import java.util.UUID;
import org.springframework.stereotype.Service;

@Service
public class CreateTask {

	private final TaskRepository taskRepository;

	private final DeletedUserRepository deletedUsers;

	public CreateTask(TaskRepository taskRepository, DeletedUserRepository deletedUsers) {
		this.taskRepository = taskRepository;
		this.deletedUsers = deletedUsers;
	}

	public TaskDto execute(UUID ownerId, Map<String, Object> input) {
		if (deletedUsers.exists(ownerId)) {
			throw new AuthenticationFailedException("Unauthorized: User account no longer exists");
		}
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
