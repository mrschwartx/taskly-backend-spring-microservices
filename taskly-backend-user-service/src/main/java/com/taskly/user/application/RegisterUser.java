package com.taskly.user.application;

import com.taskly.user.shared.domain.AlreadyExistsException;
import com.taskly.user.domain.Email;
import com.taskly.user.domain.PasswordHasher;
import com.taskly.user.domain.PlainPassword;
import com.taskly.user.domain.TokenService;
import com.taskly.user.domain.User;
import com.taskly.user.domain.UserRepository;
import com.taskly.user.domain.Username;
import java.util.Map;
import org.springframework.stereotype.Service;

@Service
public class RegisterUser {

	private final UserRepository userRepository;

	private final PasswordHasher passwordHasher;

	private final TokenService tokenService;

	public RegisterUser(UserRepository userRepository, PasswordHasher passwordHasher, TokenService tokenService) {
		this.userRepository = userRepository;
		this.passwordHasher = passwordHasher;
		this.tokenService = tokenService;
	}

	public AuthResult execute(Map<String, Object> input) {
		Username username = Username.create(input.get("username"));
		Email email = Email.create(input.get("email"));
		PlainPassword password = PlainPassword.create(input.get("password"));

		if (userRepository.existsByEmailOrUsername(email.value(), username.value(), null)) {
			throw new AlreadyExistsException("Email or Username is already registered.");
		}

		String passwordHash = passwordHasher.hash(password.value());
		User user = User.register(userRepository.nextId(), username, email, passwordHash);
		userRepository.add(user);

		return new AuthResult(UserDto.from(user), tokenService.issue(user.getId()));
	}
}
