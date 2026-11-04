package com.taskly.backend.shared.domain;

public class NotFoundException extends DomainException {

	public NotFoundException(String message) {
		super(message, "NOT_FOUND");
	}
}
