package com.live_commerce.user.domain.repository;

import java.util.List;

import com.live_commerce.user.application.dto.auth.request.UserSearchCondition;
import com.live_commerce.user.domain.model.User;

public interface UserQueryRepository {
	List<User> searchUser(UserSearchCondition condition);
}
