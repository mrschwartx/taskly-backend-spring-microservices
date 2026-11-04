package com.taskly.backend.users.infrastructure;

import static org.assertj.core.api.Assertions.assertThat;

import com.taskly.backend.users.domain.AvatarImageType;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class LocalAvatarStorageTest {

	@TempDir
	Path publicDir;

	@Test
	void storesFilesUnderTheAvatarsDirectory() throws IOException {
		LocalAvatarStorage storage = new LocalAvatarStorage(publicDir);

		String path = storage.store(AvatarImageType.PNG, new byte[] { 1, 2, 3 });

		assertThat(path).matches("^/images/users/avatar-\\d+-\\d+\\.png$");
		assertThat(Files.readAllBytes(publicDir.resolve(path.substring(1)))).containsExactly(1, 2, 3);
	}

	@Test
	void removesCustomAvatars() {
		LocalAvatarStorage storage = new LocalAvatarStorage(publicDir);
		String path = storage.store(AvatarImageType.JPEG, new byte[] { 1 });

		storage.remove(path);

		assertThat(publicDir.resolve(path.substring(1))).doesNotExist();
	}

	@Test
	void neverRemovesTheDefaultAvatarOrUnknownPaths() throws IOException {
		Path avatars = Files.createDirectories(publicDir.resolve("images/users"));
		Path defaultAvatar = Files.write(avatars.resolve("default-avatar.jpg"), new byte[] { 9 });
		LocalAvatarStorage storage = new LocalAvatarStorage(publicDir);

		storage.remove("/images/users/default-avatar.jpg");
		storage.remove(null);
		storage.remove("/images/users/avatar-../../secret");

		assertThat(defaultAvatar).exists();
	}
}
