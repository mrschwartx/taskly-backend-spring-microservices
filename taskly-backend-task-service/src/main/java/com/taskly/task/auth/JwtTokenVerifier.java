package com.taskly.task.auth;

import com.taskly.task.shared.domain.EntityIds;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import java.nio.charset.StandardCharsets;
import java.time.Clock;
import java.util.Date;
import java.util.Locale;
import java.util.UUID;
import javax.crypto.SecretKey;

public class JwtTokenVerifier implements TokenVerifier {

	private static final String ID_CLAIM = "id";

	private static final String ALGORITHM = "HS256";

	private final SecretKey key;

	private final Clock clock;

	public JwtTokenVerifier(String secret) {
		this(secret, Clock.systemUTC());
	}

	JwtTokenVerifier(String secret, Clock clock) {
		this.key = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
		this.clock = clock;
	}

	@Override
	public UUID verify(String token) {
		try {
			Jws<Claims> jws = Jwts.parser()
					.verifyWith(key)
					.clock(() -> Date.from(clock.instant()))
					.build()
					.parseSignedClaims(token);
			if (!ALGORITHM.equals(jws.getHeader().getAlgorithm())) {
				throw new InvalidTokenException("Unsupported token algorithm");
			}
			Claims claims = jws.getPayload();
			if (claims.getExpiration() == null) {
				throw new InvalidTokenException("Token has no expiration");
			}
			Object id = claims.get(ID_CLAIM);
			if (!EntityIds.isValid(id)) {
				throw new InvalidTokenException("Invalid token payload");
			}
			return UUID.fromString(((String) id).toLowerCase(Locale.ROOT));
		}
		catch (JwtException | IllegalArgumentException error) {
			throw new InvalidTokenException("Invalid token", error);
		}
	}
}
