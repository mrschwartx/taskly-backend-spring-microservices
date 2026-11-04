package com.taskly.backend.shared.domain;

public class ValidationException extends DomainException {

	public ValidationException(String message) {
		super(message, "VALIDATION_ERROR");
	}
}
