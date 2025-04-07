package com.live_commerce.user.application.service;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.live_commerce.user.application.dto.auth.request.UserSearchCondition;
import com.live_commerce.user.application.dto.auth.request.UserUpdateRequestDto;
import com.live_commerce.user.application.dto.auth.response.UserUpdateResponseDto;
import com.live_commerce.user.application.dto.user.response.UserGetResponseDto;
import com.live_commerce.user.application.exception.CustomException;
import com.live_commerce.user.application.exception.UserExceptionCode;
import com.live_commerce.user.domain.model.User;
import com.live_commerce.user.domain.repository.UserRepository;
import com.live_commerce.user.infrastructure.security.RequestUserDetails;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class UserService {

	private final UserRepository userRepository;
	private final PasswordEncoder passwordEncoder;

	@Transactional(readOnly = true)
	public UserGetResponseDto getUser(String username, RequestUserDetails userDetails) {
		validateUserGetPermission(username, userDetails);

		User user = findUserByUsername(username);

		return UserGetResponseDto.from(user);
	}

	@Transactional(readOnly = true)
	public Page<UserGetResponseDto> searchUser(UserSearchCondition condition, Pageable pageable, RequestUserDetails userDetails) {
		validateUserSearchPermission(userDetails);

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

	@Transactional
	public UserUpdateResponseDto updateUser(String username, UserUpdateRequestDto requestDto, RequestUserDetails userDetails) {
		validateUserUpdatePermission(username, requestDto, userDetails);

		User user = findUserByUsername(username);
		String updatedPassword = requestDto.getPassword() != null
			? passwordEncoder.encode(requestDto.getPassword()) : user.getPassword();

		user.updateUser(
			updatedPassword,
			requestDto.getEmail() != null ? requestDto.getEmail() : user.getEmail(),
			requestDto.getNickname() != null ? requestDto.getNickname() : user.getNickname(),
			requestDto.getAlarmConsent() != null ? requestDto.getAlarmConsent() : user.isAlarmConsent(),
			requestDto.getUserRole() != null ? requestDto.getUserRole() : user.getUserRole()
		);

		return UserUpdateResponseDto.from(user);
	}


	private void validateUserGetPermission(String username, RequestUserDetails userDetails) {
		if (!isSelf(username, userDetails) && !hasMasterRole(userDetails)) {
			throw new CustomException(UserExceptionCode.FORBIDDEN);
		}
	}

	private void validateUserSearchPermission(RequestUserDetails userDetails) {
		if (!hasMasterRole(userDetails)) {
			throw new CustomException(UserExceptionCode.FORBIDDEN);
		}
	}

	private void validateUserUpdatePermission(String username, UserUpdateRequestDto dto, RequestUserDetails userDetails) {
		if (!isSelf(username, userDetails) && !hasMasterRole(userDetails)) {
			throw new CustomException(UserExceptionCode.FORBIDDEN);
		}
		if (!hasMasterRole(userDetails) && dto.getUserRole() != null) {
			throw new CustomException(UserExceptionCode.ROLE_CHANGE_FORBIDDEN);
		}
	}

	private User findUserByUsername(String username) {
		return userRepository.findByUsername(username)
			.orElseThrow(() -> new CustomException(UserExceptionCode.USER_NOT_FOUND));
	}

	private boolean hasMasterRole(RequestUserDetails userDetails) {
		return userDetails.getAuthorities().stream()
			.anyMatch(auth -> auth.getAuthority().equals("ROLE_MASTER"));
	}

	private boolean isSelf(String username, RequestUserDetails userDetails) {
		return username.equals(userDetails.getUsername());
	}


}
