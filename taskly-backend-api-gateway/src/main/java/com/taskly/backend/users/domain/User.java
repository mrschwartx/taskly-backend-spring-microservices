package com.taskly.backend.users.domain;

import com.taskly.backend.shared.domain.Timestamps;
import java.time.Instant;
import java.util.UUID;

public class User {

	private final UUID id;

	private Username username;

	private Email email;

	private String passwordHash;

	private String avatar;

	private final Instant createdAt;

	private Instant updatedAt;

	private User(UUID id, Username username, Email email, String passwordHash, String avatar, Instant createdAt,
			Instant updatedAt) {
		this.id = id;
		this.username = username;
		this.email = email;
		this.passwordHash = passwordHash;
		this.avatar = avatar;
		this.createdAt = createdAt;
		this.updatedAt = updatedAt;
	}

	public static User register(UUID id, Username username, Email email, String passwordHash) {
		return register(id, username, email, passwordHash, Timestamps.now());
	}

	public static User register(UUID id, Username username, Email email, String passwordHash, Instant now) {
		return new User(id, username, email, passwordHash, Avatar.DEFAULT_PATH, now, now);
	}

	public static User restore(UUID id, Username username, Email email, String passwordHash, String avatar,
			Instant createdAt, Instant updatedAt) {
		return new User(id, username, email, passwordHash, avatar, createdAt, updatedAt);
	}

	public UUID getId() {
		return id;
	}

	public Username getUsername() {
		return username;
	}

	public Email getEmail() {
		return email;
	}

	public String getPasswordHash() {
		return passwordHash;
	}

	public String getAvatar() {
		return avatar;
	}

	public Instant getCreatedAt() {
		return createdAt;
	}

	public Instant getUpdatedAt() {
		return updatedAt;
	}

	public void updateProfile(Username newUsername, Email newEmail, String newPasswordHash) {
		updateProfile(newUsername, newEmail, newPasswordHash, Timestamps.now());
	}

	public void updateProfile(Username newUsername, Email newEmail, String newPasswordHash, Instant now) {
		if (newUsername != null) {
			this.username = newUsername;
		}
		if (newEmail != null) {
			this.email = newEmail;
		}
		if (newPasswordHash != null) {
			this.passwordHash = newPasswordHash;
		}
		this.updatedAt = now;
	}

	public void changeAvatar(String newAvatar) {
		changeAvatar(newAvatar, Timestamps.now());
	}

	public void changeAvatar(String newAvatar, Instant now) {
		this.avatar = newAvatar;
		this.updatedAt = now;
	}
}
