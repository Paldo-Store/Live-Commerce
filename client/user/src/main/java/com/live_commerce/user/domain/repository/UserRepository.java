package com.live_commerce.user.domain.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.live_commerce.user.domain.model.User;

public interface UserRepository extends JpaRepository<User, String>, UserQueryRepository {
	boolean existsByUsername(String username);

	boolean existsByEmail(String email);

	Optional<User> findByUsername(String username);

	Optional<User> findByEmail(String email);

	Optional<User> findByUsernameAndEmail(String username, String email);
}
