package com.taskly.backend.shared.domain;

public class AlreadyExistsException extends DomainException {

	public AlreadyExistsException(String message) {
		super(message, "ALREADY_EXISTS");
	}
}
