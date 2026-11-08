package com.taskly.user.infrastructure.messaging;

import com.taskly.user.application.OutboxMessage;
import com.taskly.user.application.OutboxStore;
import jakarta.persistence.EntityManager;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Repository
public class JpaOutboxStore implements OutboxStore {

	private final OutboxEventJpaRepository jpaRepository;

	private final EntityManager entityManager;

	public JpaOutboxStore(OutboxEventJpaRepository jpaRepository, EntityManager entityManager) {
		this.jpaRepository = jpaRepository;
		this.entityManager = entityManager;
	}

	@Override
	@Transactional(propagation = Propagation.MANDATORY)
	public void append(OutboxMessage message) {
		entityManager.persist(OutboxEventEntity.from(message));
		entityManager.flush();
	}

	@Override
	@Transactional(propagation = Propagation.MANDATORY)
	public List<OutboxMessage> lockPending(Instant now, int limit) {
		return jpaRepository.lockPending(now, limit).stream().map(OutboxEventEntity::toMessage).toList();
	}

	@Override
	@Transactional(propagation = Propagation.MANDATORY)
	public void markPublished(UUID id, Instant at) {
		OutboxEventEntity entity = entityManager.find(OutboxEventEntity.class, id);
		if (entity != null) {
			entity.markPublished(at);
		}
	}

	@Override
	@Transactional(propagation = Propagation.MANDATORY)
	public void markFailed(UUID id, int attempts, Instant nextAttemptAt) {
		OutboxEventEntity entity = entityManager.find(OutboxEventEntity.class, id);
		if (entity != null) {
			entity.markFailed(attempts, nextAttemptAt);
		}
	}

	@Override
	@Transactional
	public int deletePublishedBefore(Instant before) {
		return jpaRepository.deletePublishedBefore(before);
	}
}
