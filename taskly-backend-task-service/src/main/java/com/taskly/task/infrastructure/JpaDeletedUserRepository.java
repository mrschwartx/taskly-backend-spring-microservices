package com.taskly.task.infrastructure;

import com.taskly.task.domain.DeletedUserRepository;
import java.time.Instant;
import java.util.UUID;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
public class JpaDeletedUserRepository implements DeletedUserRepository {

	private final DeletedUserJpaRepository jpaRepository;

	public JpaDeletedUserRepository(DeletedUserJpaRepository jpaRepository) {
		this.jpaRepository = jpaRepository;
	}

	@Override
	@Transactional(readOnly = true)
	public boolean exists(UUID userId) {
		return jpaRepository.existsById(userId);
	}

	@Override
	@Transactional
	public void markDeleted(UUID userId, Instant deletedAt) {
		jpaRepository.insertIfAbsent(userId, deletedAt);
	}

	@Override
	@Transactional
	public int purgeOlderThan(Instant before) {
		return jpaRepository.deleteOlderThan(before);
	}
}
