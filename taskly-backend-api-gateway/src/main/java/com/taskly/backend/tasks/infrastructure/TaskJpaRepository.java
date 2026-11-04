package com.taskly.backend.tasks.infrastructure;

import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

interface TaskJpaRepository extends JpaRepository<TaskEntity, UUID> {

	Page<TaskEntity> findByOwnerId(UUID ownerId, Pageable pageable);

	Page<TaskEntity> findByOwnerIdAndStatus(UUID ownerId, String status, Pageable pageable);

	@Modifying
	@Query("delete from TaskEntity t where t.id = :id")
	int deleteTaskById(@Param("id") UUID id);

	@Modifying
	@Query("delete from TaskEntity t where t.ownerId = :ownerId")
	int deleteTasksByOwnerId(@Param("ownerId") UUID ownerId);
}
