package com.taskly.task.auth;

import com.taskly.task.shared.application.AuthenticatedUser;
import com.taskly.task.shared.domain.AuthenticationFailedException;
import com.taskly.task.shared.domain.ForbiddenException;
import org.springframework.stereotype.Service;

@Service
public class VerifyAccessToken {

	private final TokenVerifier tokenVerifier;

	public VerifyAccessToken(TokenVerifier tokenVerifier) {
		this.tokenVerifier = tokenVerifier;
	}

	public AuthenticatedUser execute(String token) {
		if (token == null || token.isEmpty()) {
			throw new AuthenticationFailedException("Unauthorized: Token not provided");
		}
		try {
			return new AuthenticatedUser(tokenVerifier.verify(token));
		}
		catch (InvalidTokenException error) {
			throw new ForbiddenException("Forbidden: Invalid token");
		}
	}
}
