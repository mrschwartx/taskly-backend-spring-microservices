package com.taskly.user.shared.domain;

public class ForbiddenException extends DomainException {

	public ForbiddenException(String message) {
		super(message, "FORBIDDEN");
	}
}
