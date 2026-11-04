package com.taskly.backend.shared.infrastructure.persistence;

import java.sql.SQLException;

public final class UniqueViolations {

	private static final String UNIQUE_VIOLATION_STATE = "23505";

	private UniqueViolations() {
	}

	public static boolean isUniqueViolation(Throwable error) {
		for (Throwable current = error; current != null; current = current.getCause()) {
			if (current instanceof SQLException sql && UNIQUE_VIOLATION_STATE.equals(sql.getSQLState())) {
				return true;
			}
			if (current.getCause() == current) {
				return false;
			}
		}
		return false;
	}
}
