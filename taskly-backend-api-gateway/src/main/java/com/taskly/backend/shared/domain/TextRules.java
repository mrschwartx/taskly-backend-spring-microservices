package com.taskly.backend.shared.domain;

public final class TextRules {

	private TextRules() {
	}

	public static String trim(String value) {
		int start = 0;
		int end = value.length();
		while (start < end && isTrimmable(value.charAt(start))) {
			start++;
		}
		while (end > start && isTrimmable(value.charAt(end - 1))) {
			end--;
		}
		return value.substring(start, end);
	}

	public static String parse(Object raw, String label, int minLength, int maxLength) {
		if (!(raw instanceof String text)) {
			throw new ValidationException(label + " is required");
		}
		String value = trim(text);
		if (value.length() < minLength || value.length() > maxLength) {
			throw new ValidationException(label + " must be between " + minLength + " and " + maxLength + " characters");
		}
		return value;
	}

	private static boolean isTrimmable(char character) {
		return Character.isWhitespace(character) || Character.isSpaceChar(character) || character == '\uFEFF';
	}
}
