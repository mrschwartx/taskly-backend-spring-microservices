package com.taskly.task.shared.domain;

public class AuthenticationFailedException extends DomainException {

	public AuthenticationFailedException(String message) {
		super(message, "AUTHENTICATION_FAILED");
	}
}
