package com.live_commerce.user.domain.repository;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.web.server.CoWebFilterChain;

import com.live_commerce.user.domain.model.User;
import com.netflix.appinfo.ApplicationInfoManager;

public interface UserRepository extends JpaRepository<User, UUID>, UserQueryRepository {
	boolean existsByEmail(String email);

	Optional<User> findByEmail(String email);

	Optional<User> findByUsernameAndEmail(String username, String email);

	Optional<User> findByUsername(String username);
}
