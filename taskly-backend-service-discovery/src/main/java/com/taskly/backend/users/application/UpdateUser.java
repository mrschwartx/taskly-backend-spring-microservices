package com.taskly.backend.users.application;

import com.taskly.backend.shared.domain.AlreadyExistsException;
import com.taskly.backend.shared.domain.EntityIds;
import com.taskly.backend.shared.domain.NotFoundException;
import com.taskly.backend.shared.domain.ValidationException;
import com.taskly.backend.users.domain.Email;
import com.taskly.backend.users.domain.PasswordHasher;
import com.taskly.backend.users.domain.PlainPassword;
import com.taskly.backend.users.domain.User;
import com.taskly.backend.users.domain.UserRepository;
import com.taskly.backend.users.domain.Username;
import java.util.Map;
import java.util.UUID;
import org.springframework.stereotype.Service;

@Service
public class UpdateUser {

	private final UserRepository userRepository;

	private final PasswordHasher passwordHasher;

	public UpdateUser(UserRepository userRepository, PasswordHasher passwordHasher) {
		this.userRepository = userRepository;
		this.passwordHasher = passwordHasher;
	}

	public UserDto execute(UUID requesterId, String userId, Map<String, Object> changes) {
		OwnAccount.assertOwned(requesterId, userId, "You can only update your own account");
		UUID id = EntityIds.parse(userId, "user");

		boolean hasUsername = changes.containsKey("username");
		boolean hasEmail = changes.containsKey("email");
		boolean hasPassword = changes.containsKey("password");
		if (!hasUsername && !hasEmail && !hasPassword) {
			throw new ValidationException("No updatable fields provided");
		}

		Username username = hasUsername ? Username.create(changes.get("username")) : null;
		Email email = hasEmail ? Email.create(changes.get("email")) : null;
		PlainPassword password = hasPassword ? PlainPassword.create(changes.get("password")) : null;

		User user = userRepository.findById(id).orElseThrow(() -> new NotFoundException("User not found!"));

		if (username != null || email != null) {
			boolean taken = userRepository.existsByEmailOrUsername(
					email != null ? email.value() : null,
					username != null ? username.value() : null,
					id);
			if (taken) {
				throw new AlreadyExistsException("Email or Username is already registered.");
			}
		}

		String passwordHash = password != null ? passwordHasher.hash(password.value()) : null;

		user.updateProfile(username, email, passwordHash);

		if (!userRepository.update(user)) {
			throw new NotFoundException("User not found!");
		}
		return UserDto.from(user);
	}
}
