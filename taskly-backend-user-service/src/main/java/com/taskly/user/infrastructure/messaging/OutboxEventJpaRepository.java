package com.taskly.user.infrastructure.messaging;

import java.time.Instant;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

interface OutboxEventJpaRepository extends JpaRepository<OutboxEventEntity, UUID> {

	@Query(value = """
			select * from outbox_events
			where published_at is null and next_attempt_at <= cast(:now as timestamptz)
			order by created_at asc, id asc
			limit :limit
			for update skip locked
			""", nativeQuery = true)
	List<OutboxEventEntity> lockPending(@Param("now") Instant now, @Param("limit") int limit);

	@Modifying
	@Query("delete from OutboxEventEntity e where e.publishedAt is not null and e.publishedAt < :before")
	int deletePublishedBefore(@Param("before") Instant before);
}
