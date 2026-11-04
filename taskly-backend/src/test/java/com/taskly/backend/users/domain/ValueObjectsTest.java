package com.taskly.backend.users.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.taskly.backend.shared.domain.ValidationException;
import org.junit.jupiter.api.Test;

class ValueObjectsTest {

	@Test
	void emailIsTrimmedAndValidated() {
		assertThat(Email.create("  john@mail.com ").value()).isEqualTo("john@mail.com");
		assertThatThrownBy(() -> Email.create("nope")).hasMessage("Email is invalid");
		assertThatThrownBy(() -> Email.create("a b@mail.com")).hasMessage("Email is invalid");
		assertThatThrownBy(() -> Email.create(null)).hasMessage("Email is required");
		assertThatThrownBy(() -> Email.create("a".repeat(250) + "@mail.com")).hasMessage("Email is invalid");
	}

	@Test
	void usernameLengthIsEnforcedAfterTrimming() {
		assertThat(Username.create("  john ").value()).isEqualTo("john");
		assertThatThrownBy(() -> Username.create("ab"))
				.isInstanceOf(ValidationException.class)
				.hasMessage("Username must be between 3 and 50 characters");
		assertThatThrownBy(() -> Username.create("a".repeat(51))).isInstanceOf(ValidationException.class);
		assertThatThrownBy(() -> Username.create(12)).hasMessage("Username is required");
	}

	@Test
	void passwordRulesCountCharactersAndBytes() {
		assertThat(PlainPassword.create("Password123!").value()).isEqualTo("Password123!");
		assertThatThrownBy(() -> PlainPassword.create("short")).hasMessage("Password must be at least 8 characters long");
		assertThatThrownBy(() -> PlainPassword.create("é".repeat(37))).hasMessage("Password must not exceed 72 bytes");
		assertThatThrownBy(() -> PlainPassword.create(null)).hasMessage("Password is required");
		assertThat(PlainPassword.create("Password123!").toString()).doesNotContain("Password123!");
	}

	@Test
	void avatarHelpersRecognizeCustomAvatarsOnly() {
		assertThat(Avatar.isCustom("/images/users/avatar-123-456.png")).isTrue();
		assertThat(Avatar.isCustom(Avatar.DEFAULT_PATH)).isFalse();
		assertThat(Avatar.isCustom("/images/users/avatar-../../etc")).isFalse();
		assertThat(Avatar.isCustom(null)).isFalse();
		assertThat(Avatar.toPath("avatar-1.png")).isEqualTo("/images/users/avatar-1.png");
	}

	@Test
	void avatarImageTypesMapContentTypes() {
		assertThat(AvatarImageType.fromContentType("image/PNG")).contains(AvatarImageType.PNG);
		assertThat(AvatarImageType.fromContentType("text/html")).isEmpty();
		assertThat(AvatarImageType.fromContentType(null)).isEmpty();
	}
}
