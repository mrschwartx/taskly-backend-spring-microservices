package com.taskly.backend.users.presentation;

import java.time.Duration;
import org.springframework.http.ResponseCookie;

public class AuthCookie {

	private final String name;

	private final Duration maxAge;

	private final String domain;

	private final boolean production;

	public AuthCookie(String name, Duration maxAge, String domain, boolean production) {
		this.name = name;
		this.maxAge = maxAge;
		this.domain = domain;
		this.production = production;
	}

	public String name() {
		return name;
	}

	public ResponseCookie issue(String token) {
		return build(token, maxAge);
	}

	public ResponseCookie clear() {
		return build("", Duration.ZERO);
	}

	private ResponseCookie build(String value, Duration age) {
		ResponseCookie.ResponseCookieBuilder builder = ResponseCookie.from(name, value)
				.httpOnly(true)
				.path("/")
				.maxAge(age);
		if (production) {
			builder.sameSite("None").secure(true);
			if (domain != null && !domain.isBlank()) {
				builder.domain(domain);
			}
		}
		else {
			builder.sameSite("Lax").secure(false);
		}
		return builder.build();
	}
}
