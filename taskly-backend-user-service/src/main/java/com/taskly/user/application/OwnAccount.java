package com.taskly.user.application;

import com.taskly.user.shared.domain.AuthenticationFailedException;
import java.util.Locale;
import java.util.UUID;

final class OwnAccount {

	private OwnAccount() {
	}

	static void assertOwned(UUID requesterId, String userId, String message) {
		if (userId == null || !requesterId.toString().equals(userId.toLowerCase(Locale.ROOT))) {
			throw new AuthenticationFailedException(message);
		}
	}
}
