package com.taskly.user.application;

import com.taskly.user.shared.application.AuthenticatedUser;
import com.taskly.user.shared.domain.AuthenticationFailedException;
import com.taskly.user.shared.domain.ForbiddenException;
import com.taskly.user.domain.InvalidTokenException;
import com.taskly.user.domain.TokenService;
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
