package com.taskly.task.domain;

import com.taskly.task.shared.domain.TextRules;

public record TaskDescription(String value) {

	private static final String LABEL = "Task description";

	public static TaskDescription create(Object raw) {
		return new TaskDescription(TextRules.parse(raw, LABEL, 0, 2000));
	}

	public static TaskDescription empty() {
		return new TaskDescription("");
	}

	public static TaskDescription restore(String value) {
		return new TaskDescription(value);
	}
}
