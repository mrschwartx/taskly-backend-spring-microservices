package com.taskly.backend.users.infrastructure;

import com.taskly.backend.users.domain.PasswordHasher;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

public class BcryptPasswordHasher implements PasswordHasher {

	private final BCryptPasswordEncoder encoder;

	public BcryptPasswordHasher(int rounds) {
		this.encoder = new BCryptPasswordEncoder(BCryptPasswordEncoder.BCryptVersion.$2B, rounds);
	}

	@Override
	public String hash(String plain) {
		return encoder.encode(plain);
	}

	@Override
	public boolean matches(String plain, String hash) {
		return encoder.matches(plain, hash);
	}
}
