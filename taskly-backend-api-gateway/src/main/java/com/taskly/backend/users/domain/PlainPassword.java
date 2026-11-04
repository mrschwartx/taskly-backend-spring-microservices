package com.taskly.backend.users.domain;

import com.taskly.backend.shared.domain.ValidationException;
import java.nio.charset.StandardCharsets;

public record PlainPassword(String value) {

	private static final int MIN_LENGTH = 8;

	private static final int MAX_BYTES = 72;

	public static PlainPassword create(Object raw) {
		if (!(raw instanceof String text)) {
			throw new ValidationException("Password is required");
		}
		if (text.length() < MIN_LENGTH) {
			throw new ValidationException("Password must be at least " + MIN_LENGTH + " characters long");
		}
		if (text.getBytes(StandardCharsets.UTF_8).length > MAX_BYTES) {
			throw new ValidationException("Password must not exceed " + MAX_BYTES + " bytes");
		}
		return new PlainPassword(text);
	}

	@Override
	public String toString() {
		return "PlainPassword[value=***]";
	}
}
