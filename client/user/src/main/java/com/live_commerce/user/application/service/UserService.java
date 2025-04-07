package com.live_commerce.user.application.service;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.live_commerce.user.application.dto.auth.request.UserSearchCondition;
import com.live_commerce.user.application.dto.user.response.UserGetResponseDto;
import com.live_commerce.user.application.exception.CustomException;
import com.live_commerce.user.application.exception.UserExceptionCode;
import com.live_commerce.user.domain.model.User;
import com.live_commerce.user.domain.repository.UserRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class UserService {

	private final UserRepository userRepository;

	@Transactional(readOnly = true)
	public UserGetResponseDto getUser(String username) {
		User user = findUserByUsername(username);

		return UserGetResponseDto.from(user);
	}

	@Transactional(readOnly = true)
	public Page<UserGetResponseDto> searchUser(UserSearchCondition condition, Pageable pageable) {
		int size = pageable.getPageSize();
		if (size != 10 && size != 30 && size != 50) {
			pageable = PageRequest.of(pageable.getPageNumber(), 10, pageable.getSort());
		}

		List<User> users = userRepository.searchUser(condition);
		List<UserGetResponseDto> dtoList = users.stream()
			.map(UserGetResponseDto::from)
			.toList();

		return new PageImpl<>(dtoList, pageable, dtoList.size());
	}

	private User findUserByUsername(String username) {
		return userRepository.findByUsername(username)
			.orElseThrow(() -> new CustomException(UserExceptionCode.USER_NOT_FOUND));
	}
}
