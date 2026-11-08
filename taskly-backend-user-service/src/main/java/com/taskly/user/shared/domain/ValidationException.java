package com.taskly.user.shared.domain;

public class ValidationException extends DomainException {

	public ValidationException(String message) {
		super(message, "VALIDATION_ERROR");
	}
}
