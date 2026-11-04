package com.taskly.backend.users.application;

import com.taskly.backend.shared.application.AuthenticatedUser;
import com.taskly.backend.shared.domain.AuthenticationFailedException;
import com.taskly.backend.shared.domain.ForbiddenException;
import com.taskly.backend.users.domain.InvalidTokenException;
import com.taskly.backend.users.domain.TokenService;
import org.springframework.stereotype.Service;

@Service
public class VerifyAccessToken {

	private final TokenService tokenService;

	public VerifyAccessToken(TokenService tokenService) {
		this.tokenService = tokenService;
	}

	public AuthenticatedUser execute(String token) {
		if (token == null || token.isEmpty()) {
			throw new AuthenticationFailedException("Unauthorized: Token not provided");
		}
		try {
			return new AuthenticatedUser(tokenService.verify(token));
		}
		catch (InvalidTokenException error) {
			throw new ForbiddenException("Forbidden: Invalid token");
		}
	}
}
