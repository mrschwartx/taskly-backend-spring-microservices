package com.taskly.backend.users.infrastructure;

import com.taskly.backend.users.application.AvatarStorage;
import com.taskly.backend.users.domain.Avatar;
import com.taskly.backend.users.domain.AvatarImageType;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.security.SecureRandom;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class LocalAvatarStorage implements AvatarStorage {

	private static final Logger log = LoggerFactory.getLogger(LocalAvatarStorage.class);

	private static final int RANDOM_BOUND = 1_000_000_000;

	private final Path publicDir;

	private final Path avatarsDir;

	private final SecureRandom random = new SecureRandom();

	public LocalAvatarStorage(Path publicDir) {
		this.publicDir = publicDir.toAbsolutePath().normalize();
		this.avatarsDir = this.publicDir.resolve("images").resolve("users");
	}

	@Override
	public String store(AvatarImageType type, byte[] content) {
		String filename = "avatar-" + System.currentTimeMillis() + "-" + random.nextInt(RANDOM_BOUND)
				+ type.extension();
		try {
			Files.createDirectories(avatarsDir);
			Files.write(avatarsDir.resolve(filename), content, StandardOpenOption.CREATE_NEW);
		}
		catch (IOException error) {
			throw new UncheckedIOException("Failed to store avatar file", error);
		}
		return Avatar.toPath(filename);
	}

	@Override
	public void remove(String avatarPath) {
		if (!Avatar.isCustom(avatarPath)) {
			return;
		}
		Path target = publicDir.resolve(avatarPath.substring(1)).normalize();
		if (!target.startsWith(avatarsDir)) {
			return;
		}
		try {
			Files.deleteIfExists(target);
		}
		catch (IOException error) {
			log.warn("Failed to remove avatar file {}: {}", avatarPath, error.getMessage());
		}
	}
}
