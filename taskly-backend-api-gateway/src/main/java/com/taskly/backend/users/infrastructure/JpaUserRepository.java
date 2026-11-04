package com.taskly.backend.users.infrastructure;

import com.taskly.backend.shared.domain.AlreadyExistsException;
import com.taskly.backend.shared.domain.EntityIds;
import com.taskly.backend.shared.infrastructure.persistence.UniqueViolations;
import com.taskly.backend.users.domain.User;
import com.taskly.backend.users.domain.UserRepository;
import jakarta.persistence.EntityManager;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
public class JpaUserRepository implements UserRepository {

	private static final String DUPLICATE_MESSAGE = "Email or Username is already registered.";

	private static final UUID NO_EXCLUSION = new UUID(0L, 0L);

	private final UserJpaRepository jpaRepository;

	private final EntityManager entityManager;

	public JpaUserRepository(UserJpaRepository jpaRepository, EntityManager entityManager) {
		this.jpaRepository = jpaRepository;
		this.entityManager = entityManager;
	}

	@Override
	public UUID nextId() {
		return EntityIds.generate();
	}

	@Override
	@Transactional
	public void add(User user) {
		try {
			entityManager.persist(UserEntity.from(user));
			entityManager.flush();
		}
		catch (RuntimeException error) {
			throw translate(error);
		}
	}

	@Override
	@Transactional
	public boolean update(User user) {
		UserEntity entity = entityManager.find(UserEntity.class, user.getId());
		if (entity == null) {
			return false;
		}
		try {
			entity.apply(user);
			entityManager.flush();
			return true;
		}
		catch (RuntimeException error) {
			throw translate(error);
		}
	}

	@Override
	public Optional<User> findById(UUID id) {
		return jpaRepository.findById(id).map(UserEntity::toDomain);
	}

	@Override
	public Optional<User> findByEmail(String email) {
		return jpaRepository.findByEmailIgnoringCase(email).map(UserEntity::toDomain);
	}

	@Override
	public boolean existsByEmailOrUsername(String email, String username, UUID excludeId) {
		UUID excluded = excludeId != null ? excludeId : NO_EXCLUSION;
		if (email != null && jpaRepository.countByEmailExcluding(email, excluded) > 0) {
			return true;
		}
		return username != null && jpaRepository.countByUsernameExcluding(username, excluded) > 0;
	}

	@Override
	public List<User> search(String username, String email) {
		return jpaRepository.search(likePattern(username), likePattern(email))
				.stream()
				.map(UserEntity::toDomain)
				.toList();
	}

	@Override
	@Transactional
	public boolean deleteById(UUID id) {
		return jpaRepository.deleteUserById(id) == 1;
	}

	private static String likePattern(String value) {
		if (value == null || value.isEmpty()) {
			return null;
		}
		return "%" + value.replaceAll("[\\\\%_]", "\\\\$0") + "%";
	}

	private static RuntimeException translate(RuntimeException error) {
		if (UniqueViolations.isUniqueViolation(error)) {
			return new AlreadyExistsException(DUPLICATE_MESSAGE);
		}
		return error;
	}
}
