package com.taskly.user.support;

import com.taskly.user.domain.InvalidTokenException;
import com.taskly.user.domain.TokenService;
import java.util.UUID;

public class FakeTokenService implements TokenService {

	@Override
	public String issue(UUID userId) {
		return "token:" + userId;
	}

	@Override
	public UUID verify(String token) {
		if (!token.startsWith("token:")) {
			throw new InvalidTokenException("bad token");
		}
		return UUID.fromString(token.substring("token:".length()));
	}
}
