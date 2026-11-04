package com.taskly.backend.users.domain;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Instant;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class UserTest {

	private static final Instant CREATED = Instant.parse("2026-01-01T00:00:00Z");

	private User newUser() {
		return User.register(UUID.randomUUID(), Username.create("john"), Email.create("john@mail.com"), "hash",
				CREATED);
	}

	@Test
	void registersWithDefaultAvatarAndEqualTimestamps() {
		User user = newUser();
		assertThat(user.getAvatar()).isEqualTo(Avatar.DEFAULT_PATH);
		assertThat(user.getCreatedAt()).isEqualTo(user.getUpdatedAt());
	}

	@Test
	void updateProfileChangesOnlyProvidedFields() {
		User user = newUser();
		Instant later = CREATED.plusSeconds(60);

		user.updateProfile(Username.create("johnny"), null, null, later);

		assertThat(user.getUsername().value()).isEqualTo("johnny");
		assertThat(user.getEmail().value()).isEqualTo("john@mail.com");
		assertThat(user.getPasswordHash()).isEqualTo("hash");
		assertThat(user.getUpdatedAt()).isEqualTo(later);
		assertThat(user.getCreatedAt()).isEqualTo(CREATED);
	}

	@Test
	void changeAvatarTouchesTheUser() {
		User user = newUser();
		Instant later = CREATED.plusSeconds(5);

		user.changeAvatar("/images/users/avatar-1.png", later);

		assertThat(user.getAvatar()).isEqualTo("/images/users/avatar-1.png");
		assertThat(user.getUpdatedAt()).isEqualTo(later);
	}
}
