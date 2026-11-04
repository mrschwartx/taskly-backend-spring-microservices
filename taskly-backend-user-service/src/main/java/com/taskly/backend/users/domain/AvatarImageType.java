package com.taskly.backend.users.domain;

import java.util.Arrays;
import java.util.Locale;
import java.util.Optional;

public enum AvatarImageType {

	JPEG("image/jpeg", ".jpg"),
	PNG("image/png", ".png"),
	WEBP("image/webp", ".webp"),
	GIF("image/gif", ".gif");

	private final String contentType;

	private final String extension;

	AvatarImageType(String contentType, String extension) {
		this.contentType = contentType;
		this.extension = extension;
	}

	public String contentType() {
		return contentType;
	}

	public String extension() {
		return extension;
	}

	public static Optional<AvatarImageType> fromContentType(String contentType) {
		if (contentType == null) {
			return Optional.empty();
		}
		String normalized = contentType.toLowerCase(Locale.ROOT);
		return Arrays.stream(values()).filter(type -> type.contentType.equals(normalized)).findFirst();
	}
}
