package com.taskly.task.auth;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.taskly.task.shared.application.AuthenticatedUser;
import com.taskly.task.shared.domain.AuthenticationFailedException;
import com.taskly.task.shared.domain.ForbiddenException;
import com.taskly.task.support.TestTokens;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class JwtTokenVerifierTest {

	private static final String SECRET = "0123456789abcdef0123456789abcdef";

	private static final Instant NOW = Instant.parse("2026-10-25T00:00:00Z");

	private static JwtTokenVerifier verifier(String secret, Instant now) {
		return new JwtTokenVerifier(secret, Clock.fixed(now, ZoneOffset.UTC));
	}

	@Test
	void verifiesTokensIssuedByTheUserService() {
		UUID userId = UUID.randomUUID();
		String token = TestTokens.issue(SECRET, userId, NOW, Duration.ofHours(1));

		assertThat(verifier(SECRET, NOW).verify(token)).isEqualTo(userId);
	}

	@Test
	void rejectsExpiredTokens() {
		String token = TestTokens.issue(SECRET, UUID.randomUUID(), NOW, Duration.ofHours(1));

		assertThatThrownBy(() -> verifier(SECRET, NOW.plus(Duration.ofHours(2))).verify(token))
				.isInstanceOf(InvalidTokenException.class);
	}

	@Test
	void rejectsTokensSignedWithAnotherSecret() {
		String token = TestTokens.issue("fedcba9876543210fedcba9876543210", UUID.randomUUID(), NOW,
				Duration.ofHours(1));

		assertThatThrownBy(() -> verifier(SECRET, NOW).verify(token)).isInstanceOf(InvalidTokenException.class);
	}

	@Test
	void rejectsTamperedAndMalformedTokens() {
		JwtTokenVerifier verifier = verifier(SECRET, NOW);
		String token = TestTokens.issue(SECRET, UUID.randomUUID(), NOW, Duration.ofHours(1));
		String tampered = token.substring(0, token.length() - 2) + (token.endsWith("AA") ? "BB" : "AA");

		assertThatThrownBy(() -> verifier.verify(tampered)).isInstanceOf(InvalidTokenException.class);
		assertThatThrownBy(() -> verifier.verify("garbage")).isInstanceOf(InvalidTokenException.class);
		assertThatThrownBy(() -> verifier.verify("a.b.c")).isInstanceOf(InvalidTokenException.class);
	}

	@Test
	void verifyAccessTokenMapsFailuresToTheMonolithContract() {
		UUID userId = UUID.randomUUID();
		VerifyAccessToken verify = new VerifyAccessToken(verifier(SECRET, NOW));
		String token = TestTokens.issue(SECRET, userId, NOW, Duration.ofHours(1));

		assertThat(verify.execute(token)).isEqualTo(new AuthenticatedUser(userId));
		assertThatThrownBy(() -> verify.execute(null)).isInstanceOf(AuthenticationFailedException.class)
				.hasMessage("Unauthorized: Token not provided");
		assertThatThrownBy(() -> verify.execute("")).isInstanceOf(AuthenticationFailedException.class);
		assertThatThrownBy(() -> verify.execute("garbage")).isInstanceOf(ForbiddenException.class)
				.hasMessage("Forbidden: Invalid token");
	}
}
