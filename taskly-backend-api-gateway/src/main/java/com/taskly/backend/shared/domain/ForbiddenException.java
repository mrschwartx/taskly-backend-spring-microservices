package com.taskly.backend.shared.domain;

public class ForbiddenException extends DomainException {

	public ForbiddenException(String message) {
		super(message, "FORBIDDEN");
	}
}
