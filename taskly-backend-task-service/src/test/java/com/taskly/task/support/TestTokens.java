package com.taskly.task.support;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.Instant;
import java.util.Date;
import java.util.UUID;
import javax.crypto.SecretKey;

public final class TestTokens {

	private TestTokens() {
	}

	public static String issue(String secret, UUID userId, Instant now, Duration ttl) {
		SecretKey key = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
		return Jwts.builder()
				.claim("id", userId.toString())
				.issuedAt(Date.from(now))
				.expiration(Date.from(now.plus(ttl)))
				.signWith(key, Jwts.SIG.HS256)
				.compact();
	}
}
