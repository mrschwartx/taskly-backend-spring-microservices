package com.taskly.backend.users.domain;

import com.taskly.backend.shared.domain.TextRules;
import com.taskly.backend.shared.domain.ValidationException;

public record Username(String value) {

	private static final int MIN_LENGTH = 3;

	private static final int MAX_LENGTH = 50;

	public static Username create(Object raw) {
		if (!(raw instanceof String)) {
			throw new ValidationException("Username is required");
		}
		return new Username(TextRules.parse(raw, "Username", MIN_LENGTH, MAX_LENGTH));
	}

	public static Username restore(String value) {
		return new Username(value);
	}
}
