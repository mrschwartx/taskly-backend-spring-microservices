package com.taskly.backend.support;

import com.taskly.backend.shared.domain.EntityIds;
import com.taskly.backend.tasks.domain.Task;
import com.taskly.backend.tasks.domain.TaskPage;
import com.taskly.backend.tasks.domain.TaskRepository;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

public class InMemoryTaskRepository implements TaskRepository {

	private final Map<UUID, Task> tasks = new LinkedHashMap<>();

	@Override
	public UUID nextId() {
		return EntityIds.generate();
	}

	@Override
	public void add(Task task) {
		tasks.put(task.getId(), task);
	}

	@Override
	public boolean update(Task task) {
		if (!tasks.containsKey(task.getId())) {
			return false;
		}
		tasks.put(task.getId(), task);
		return true;
	}

	@Override
	public Optional<Task> findById(UUID id) {
		return Optional.ofNullable(tasks.get(id));
	}

	@Override
	public TaskPage findByOwner(UUID ownerId, String status, String orderBy, int page, int pageSize) {
		List<Task> owned = new ArrayList<>(tasks.values().stream()
				.filter(task -> task.getOwnerId().equals(ownerId))
				.filter(task -> status == null || task.getStatus().value().equals(status))
				.toList());
		if ("name".equals(orderBy)) {
			owned.sort(Comparator.comparing(task -> task.getName().value()));
		}
		int from = Math.min((page - 1) * pageSize, owned.size());
		int to = Math.min(from + pageSize, owned.size());
		return new TaskPage(owned.subList(from, to), owned.size());
	}

	@Override
	public boolean deleteById(UUID id) {
		return tasks.remove(id) != null;
	}

	@Override
	public int deleteByOwner(UUID ownerId) {
		int before = tasks.size();
		tasks.values().removeIf(task -> task.getOwnerId().equals(ownerId));
		return before - tasks.size();
	}
}
