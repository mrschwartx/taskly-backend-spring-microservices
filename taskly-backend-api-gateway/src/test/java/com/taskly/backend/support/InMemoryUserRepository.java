package com.taskly.backend.support;

import com.taskly.backend.shared.domain.AlreadyExistsException;
import com.taskly.backend.shared.domain.EntityIds;
import com.taskly.backend.users.domain.User;
import com.taskly.backend.users.domain.UserRepository;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

public class InMemoryUserRepository implements UserRepository {

	private final Map<UUID, User> users = new LinkedHashMap<>();

	@Override
	public UUID nextId() {
		return EntityIds.generate();
	}

	@Override
	public void add(User user) {
		if (existsByEmailOrUsername(user.getEmail().value(), user.getUsername().value(), null)) {
			throw new AlreadyExistsException("Email or Username is already registered.");
		}
		users.put(user.getId(), user);
	}

	@Override
	public boolean update(User user) {
		if (!users.containsKey(user.getId())) {
			return false;
		}
		users.put(user.getId(), user);
		return true;
	}

	@Override
	public Optional<User> findById(UUID id) {
		return Optional.ofNullable(users.get(id));
	}

	@Override
	public Optional<User> findByEmail(String email) {
		return users.values().stream().filter(user -> user.getEmail().value().equalsIgnoreCase(email)).findFirst();
	}

	@Override
	public boolean existsByEmailOrUsername(String email, String username, UUID excludeId) {
		return users.values().stream()
				.filter(user -> !user.getId().equals(excludeId))
				.anyMatch(user -> (email != null && user.getEmail().value().equalsIgnoreCase(email))
						|| (username != null && user.getUsername().value().equalsIgnoreCase(username)));
	}

	@Override
	public List<User> search(String username, String email) {
		List<User> result = new ArrayList<>();
		for (User user : users.values()) {
			boolean usernameMatches = username == null || username.isEmpty()
					|| user.getUsername().value().toLowerCase(Locale.ROOT).contains(username.toLowerCase(Locale.ROOT));
			boolean emailMatches = email == null || email.isEmpty()
					|| user.getEmail().value().toLowerCase(Locale.ROOT).contains(email.toLowerCase(Locale.ROOT));
			if (usernameMatches && emailMatches) {
				result.add(user);
			}
		}
		return result;
	}

	@Override
	public boolean deleteById(UUID id) {
		return users.remove(id) != null;
	}
}
