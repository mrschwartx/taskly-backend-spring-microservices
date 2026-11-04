package com.taskly.backend.users.domain;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface UserRepository {

	UUID nextId();

	void add(User user);

	boolean update(User user);

	Optional<User> findById(UUID id);

	Optional<User> findByEmail(String email);

	boolean existsByEmailOrUsername(String email, String username, UUID excludeId);

	List<User> search(String username, String email);

	boolean deleteById(UUID id);
}
