package com.taskly.backend.users.infrastructure;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

interface UserJpaRepository extends JpaRepository<UserEntity, UUID> {

	@Query("select u from UserEntity u where lower(u.email) = lower(:email)")
	Optional<UserEntity> findByEmailIgnoringCase(@Param("email") String email);

	@Query("select count(u) from UserEntity u where lower(u.email) = lower(:email) and u.id <> :excludeId")
	long countByEmailExcluding(@Param("email") String email, @Param("excludeId") UUID excludeId);

	@Query("select count(u) from UserEntity u where lower(u.username) = lower(:username) and u.id <> :excludeId")
	long countByUsernameExcluding(@Param("username") String username, @Param("excludeId") UUID excludeId);

	@Query(value = """
			select * from users
			where (cast(:username as text) is null or username ilike cast(:username as text))
			  and (cast(:email as text) is null or email ilike cast(:email as text))
			order by created_at asc, id asc
			""", nativeQuery = true)
	List<UserEntity> search(@Param("username") String username, @Param("email") String email);

	@Modifying
	@Query("delete from UserEntity u where u.id = :id")
	int deleteUserById(@Param("id") UUID id);
}
