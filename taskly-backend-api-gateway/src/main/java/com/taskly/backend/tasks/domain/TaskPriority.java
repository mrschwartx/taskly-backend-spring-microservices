package com.taskly.backend.tasks.domain;

import com.taskly.backend.shared.domain.TextRules;
import com.taskly.backend.shared.domain.ValidationException;
import java.util.Arrays;
import java.util.stream.Collectors;

public enum TaskPriority {

	LOW("low"),
	MEDIUM("medium"),
	HIGH("high");

	private static final String LABEL = "Task priority";

	private final String value;

	TaskPriority(String value) {
		this.value = value;
	}

	public String value() {
		return value;
	}

	public static TaskPriority create(Object raw) {
		String candidate = TextRules.parse(raw, LABEL, 1, 50);
		return Arrays.stream(values())
				.filter(priority -> priority.value.equals(candidate))
				.findFirst()
				.orElseThrow(() -> new ValidationException(LABEL + " must be one of: " + allowedValues()));
	}

	public static TaskPriority restore(String value) {
		return Arrays.stream(values())
				.filter(priority -> priority.value.equals(value))
				.findFirst()
				.orElseThrow(() -> new IllegalArgumentException("Unknown task priority: " + value));
	}

	private static String allowedValues() {
		return Arrays.stream(values()).map(TaskPriority::value).collect(Collectors.joining(", "));
	}
}
