package com.taskly.user.domain;

public interface PasswordHasher {

	String hash(String plain);

	boolean matches(String plain, String hash);
}
