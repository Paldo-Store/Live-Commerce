package com.live_commerce.user.application.service;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.live_commerce.user.application.dto.auth.request.UserSignUpRequestDto;
import com.live_commerce.user.application.dto.auth.response.UserSignUpResponseDto;
import com.live_commerce.user.application.exception.CustomException;
import com.live_commerce.user.application.exception.UserExceptionCode;
import com.live_commerce.user.domain.model.User;
import com.live_commerce.user.domain.repository.UserRepository;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AuthService {

	private final UserRepository userRepository;
	private final PasswordEncoder passwordEncoder;

	@Transactional
	public UserSignUpResponseDto signUp(UserSignUpRequestDto request) {

		validateUsername(request.getUsername());
		validateEmail(request.getEmail());

		// 비밀번호 암호화
		String encodedPassword = passwordEncoder.encode(request.getPassword());

		// DTO가 암호화된 비밀번호 받아서 엔티티 생성
		User user = request.toEntity(encodedPassword);

		User savedUser = userRepository.save(user);

		return UserSignUpResponseDto.from(savedUser);
	}

	private void validateUsername(String username) {
		if (userRepository.existsByUsername(username)) {
			throw new CustomException(UserExceptionCode.DUPLICATE_USERNAME);
		}
	}

	private void validateEmail(String email) {
		if (userRepository.existsByEmail(email)) {
			throw new CustomException(UserExceptionCode.DUPLICATE_EMAIL);
		}
	}


}
