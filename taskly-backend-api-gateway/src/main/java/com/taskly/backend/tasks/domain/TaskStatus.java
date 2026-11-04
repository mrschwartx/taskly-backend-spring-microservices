package com.taskly.backend.tasks.domain;

import com.taskly.backend.shared.domain.TextRules;
import com.taskly.backend.shared.domain.ValidationException;
import java.util.Arrays;
import java.util.stream.Collectors;

public enum TaskStatus {

	TODO("todo"),
	IN_PROGRESS("in_progress"),
	DONE("done");

	private static final String LABEL = "Task status";

	private final String value;

	TaskStatus(String value) {
		this.value = value;
	}

	public String value() {
		return value;
	}

	public static TaskStatus defaultStatus() {
		return TODO;
	}

	public static TaskStatus create(Object raw) {
		String candidate = TextRules.parse(raw, LABEL, 1, 50);
		return Arrays.stream(values())
				.filter(status -> status.value.equals(candidate))
				.findFirst()
				.orElseThrow(() -> new ValidationException(LABEL + " must be one of: " + allowedValues()));
	}

	public static TaskStatus restore(String value) {
		return Arrays.stream(values())
				.filter(status -> status.value.equals(value))
				.findFirst()
				.orElseThrow(() -> new IllegalArgumentException("Unknown task status: " + value));
	}

	private static String allowedValues() {
		return Arrays.stream(values()).map(TaskStatus::value).collect(Collectors.joining(", "));
	}
}
