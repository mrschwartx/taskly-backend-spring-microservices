package com.taskly.task.infrastructure;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "deleted_users")
public class DeletedUserEntity {

	@Id
	@Column(name = "user_id", nullable = false, updatable = false)
	private UUID userId;

	@Column(name = "deleted_at", nullable = false, updatable = false)
	private Instant deletedAt;

	protected DeletedUserEntity() {
	}
}
