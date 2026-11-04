package com.taskly.backend.users.domain;

import com.taskly.backend.shared.domain.TextRules;
import com.taskly.backend.shared.domain.ValidationException;
import java.util.regex.Pattern;

public record Email(String value) {

	private static final Pattern EMAIL_PATTERN = Pattern.compile("^[^\\s@]+@[^\\s@]+\\.[^\\s@]+$",
			Pattern.UNICODE_CHARACTER_CLASS);

	private static final int MAX_LENGTH = 254;

	public static Email create(Object raw) {
		if (!(raw instanceof String text)) {
			throw new ValidationException("Email is required");
		}
		String value = TextRules.trim(text);
		if (value.isEmpty() || value.length() > MAX_LENGTH || !EMAIL_PATTERN.matcher(value).matches()) {
			throw new ValidationException("Email is invalid");
		}
		return new Email(value);
	}

	public static Email restore(String value) {
		return new Email(value);
	}
}
