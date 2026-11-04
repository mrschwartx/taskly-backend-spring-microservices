package com.taskly.backend.tasks.domain;

import com.taskly.backend.shared.domain.TextRules;

public record TaskName(String value) {

	private static final String LABEL = "Task name";

	public static TaskName create(Object raw) {
		return new TaskName(TextRules.parse(raw, LABEL, 1, 120));
	}

	public static TaskName restore(String value) {
		return new TaskName(value);
	}
}
