package com.taskly.backend.users.application;

import com.taskly.backend.shared.domain.NotFoundException;
import com.taskly.backend.shared.domain.ValidationException;
import com.taskly.backend.users.domain.User;
import com.taskly.backend.users.domain.UserRepository;
import java.util.List;
import org.springframework.stereotype.Service;

@Service
public class ListUsers {

	private final UserRepository userRepository;

	public ListUsers(UserRepository userRepository) {
		this.userRepository = userRepository;
	}

	public List<UserDto> execute(Object username, Object email) {
		String usernameFilter = optionalString(username, "Username");
		String emailFilter = optionalString(email, "Email");

		List<User> users = userRepository.search(usernameFilter, emailFilter);
		if (users.isEmpty()) {
			throw new NotFoundException("No users found");
		}
		return users.stream().map(UserDto::from).toList();
	}

	private static String optionalString(Object value, String label) {
		if (value == null) {
			return null;
		}
		if (!(value instanceof String text)) {
			throw new ValidationException(label + " filter must be a string");
		}
		return text;
	}
}
