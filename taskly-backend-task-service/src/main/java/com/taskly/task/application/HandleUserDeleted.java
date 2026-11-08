package com.taskly.task.application;

import com.taskly.task.domain.DeletedUserRepository;
import com.taskly.task.domain.TaskRepository;
import com.taskly.task.shared.domain.EntityIds;
import com.taskly.task.shared.domain.Timestamps;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class HandleUserDeleted {

	private final TaskRepository taskRepository;

	private final DeletedUserRepository deletedUsers;

	public HandleUserDeleted(TaskRepository taskRepository, DeletedUserRepository deletedUsers) {
		this.taskRepository = taskRepository;
		this.deletedUsers = deletedUsers;
	}

	@Transactional
	public int execute(Object userId) {
		UUID id = EntityIds.parse(userId, "user");
		deletedUsers.markDeleted(id, Timestamps.now());
		return taskRepository.deleteByOwner(id);
	}
}
