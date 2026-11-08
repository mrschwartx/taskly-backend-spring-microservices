package com.taskly.user.application;

public class MessagePublishException extends RuntimeException {

	public MessagePublishException(String message) {
		super(message);
	}

	public MessagePublishException(String message, Throwable cause) {
		super(message, cause);
	}
}
