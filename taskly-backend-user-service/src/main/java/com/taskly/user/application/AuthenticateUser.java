package com.taskly.user.application;

import com.taskly.user.shared.domain.AuthenticationFailedException;
import com.taskly.user.shared.domain.NotFoundException;
import com.taskly.user.shared.domain.TextRules;
import com.taskly.user.shared.domain.ValidationException;
import com.taskly.user.domain.PasswordHasher;
import com.taskly.user.domain.TokenService;
import com.taskly.user.domain.User;
import com.taskly.user.domain.UserRepository;
import java.util.Map;
import org.springframework.stereotype.Service;

@Service
public class AuthenticateUser {

	private final UserRepository userRepository;

	private final PasswordHasher passwordHasher;

	private final TokenService tokenService;

	public AuthenticateUser(UserRepository userRepository, PasswordHasher passwordHasher, TokenService tokenService) {
		this.userRepository = userRepository;
		this.passwordHasher = passwordHasher;
		this.tokenService = tokenService;
	}

	public AuthResult execute(Map<String, Object> input) {
		if (!(input.get("email") instanceof String email) || !(input.get("password") instanceof String password)) {
			throw new ValidationException("Email and password are required");
		}

		User user = userRepository.findByEmail(TextRules.trim(email))
				.orElseThrow(() -> new NotFoundException("User not found!"));

		if (!passwordHasher.matches(password, user.getPasswordHash())) {
			throw new AuthenticationFailedException("Wrong password!");
		}

		return new AuthResult(UserDto.from(user), tokenService.issue(user.getId()));
	}
}
