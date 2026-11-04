package com.taskly.backend.support;

import com.taskly.backend.users.domain.PasswordHasher;

public class FakePasswordHasher implements PasswordHasher {

	@Override
	public String hash(String plain) {
		return "hashed:" + plain;
	}

	@Override
	public boolean matches(String plain, String hash) {
		return hash(plain).equals(hash);
	}
}
