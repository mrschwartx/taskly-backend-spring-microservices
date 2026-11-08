package com.taskly.task.infrastructure;

import java.time.Instant;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

interface DeletedUserJpaRepository extends JpaRepository<DeletedUserEntity, UUID> {

	@Modifying
	@Query(value = """
			insert into deleted_users (user_id, deleted_at)
			values (:userId, cast(:deletedAt as timestamptz))
			on conflict (user_id) do nothing
			""", nativeQuery = true)
	int insertIfAbsent(@Param("userId") UUID userId, @Param("deletedAt") Instant deletedAt);

	@Modifying
	@Query("delete from DeletedUserEntity d where d.deletedAt < :before")
	int deleteOlderThan(@Param("before") Instant before);
}
