package com.taskly.user.support;

import com.taskly.user.domain.PasswordHasher;

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
