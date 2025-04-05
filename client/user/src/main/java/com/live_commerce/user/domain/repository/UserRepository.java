package com.live_commerce.user.domain.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.live_commerce.user.domain.model.User;

public interface UserRepository extends JpaRepository<User, String> {
	boolean existsByUsername(String username);

	boolean existsByEmail(String email);
}
