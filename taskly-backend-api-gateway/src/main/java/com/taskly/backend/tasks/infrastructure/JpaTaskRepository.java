package com.taskly.backend.tasks.infrastructure;

import com.taskly.backend.shared.domain.EntityIds;
import com.taskly.backend.tasks.domain.Task;
import com.taskly.backend.tasks.domain.TaskPage;
import com.taskly.backend.tasks.domain.TaskRepository;
import jakarta.persistence.EntityManager;
import java.util.LinkedHashSet;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
public class JpaTaskRepository implements TaskRepository {

	private final TaskJpaRepository jpaRepository;

	private final EntityManager entityManager;

	public JpaTaskRepository(TaskJpaRepository jpaRepository, EntityManager entityManager) {
		this.jpaRepository = jpaRepository;
		this.entityManager = entityManager;
	}

	@Override
	public UUID nextId() {
		return EntityIds.generate();
	}

	@Override
	@Transactional
	public void add(Task task) {
		entityManager.persist(TaskEntity.from(task));
		entityManager.flush();
	}

	@Override
	@Transactional
	public boolean update(Task task) {
		TaskEntity entity = entityManager.find(TaskEntity.class, task.getId());
		if (entity == null) {
			return false;
		}
		entity.apply(task);
		entityManager.flush();
		return true;
	}

	@Override
	public Optional<Task> findById(UUID id) {
		return jpaRepository.findById(id).map(TaskEntity::toDomain);
	}

	@Override
	public TaskPage findByOwner(UUID ownerId, String status, String orderBy, int page, int pageSize) {
		PageRequest pageRequest = PageRequest.of(page - 1, pageSize, sortFor(orderBy));
		Page<TaskEntity> result = status != null
				? jpaRepository.findByOwnerIdAndStatus(ownerId, status, pageRequest)
				: jpaRepository.findByOwnerId(ownerId, pageRequest);
		return new TaskPage(result.getContent().stream().map(TaskEntity::toDomain).toList(),
				result.getTotalElements());
	}

	@Override
	@Transactional
	public boolean deleteById(UUID id) {
		return jpaRepository.deleteTaskById(id) == 1;
	}

	@Override
	@Transactional
	public int deleteByOwner(UUID ownerId) {
		return jpaRepository.deleteTasksByOwnerId(ownerId);
	}

	private static Sort sortFor(String orderBy) {
		Set<String> properties = new LinkedHashSet<>();
		if (orderBy != null) {
			properties.add(orderBy);
		}
		properties.add("createdAt");
		properties.add("id");
		return Sort.by(Sort.Direction.ASC, properties.toArray(String[]::new));
	}
}
