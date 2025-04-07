package com.live_commerce.user.application.service;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.live_commerce.user.application.dto.auth.request.UserSignInRequestDto;
import com.live_commerce.user.application.dto.auth.request.UserSignUpRequestDto;
import com.live_commerce.user.application.dto.auth.response.UserSignInResponseDto;
import com.live_commerce.user.application.dto.auth.response.UserSignUpResponseDto;
import com.live_commerce.user.application.exception.CustomException;
import com.live_commerce.user.application.exception.UserExceptionCode;
import com.live_commerce.user.domain.model.User;
import com.live_commerce.user.domain.repository.UserRepository;
import com.live_commerce.user.infrastructure.common.JwtUtil;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AuthService {

	private final UserRepository userRepository;
	private final PasswordEncoder passwordEncoder;
	private final JwtUtil jwtUtil;

	@Transactional
	public UserSignUpResponseDto signUp(UserSignUpRequestDto request) {

		validateUsername(request.username());
		validateEmail(request.email());

		String encodedPassword = passwordEncoder.encode(request.password());

		User user = request.toEntity(encodedPassword);

		User savedUser = userRepository.save(user);

		return UserSignUpResponseDto.from(savedUser);
	}

	@Transactional
	public UserSignInResponseDto signIn(UserSignInRequestDto requestDto) {

		User user = userRepository.findByUsername(requestDto.username())
			.filter(u -> passwordEncoder.matches(requestDto.password(), u.getPassword()))
			.orElseThrow(() -> new CustomException(UserExceptionCode.INVALID_CREDENTIALS));

		checkDeletedUser(user);

		String accessToken = jwtUtil.createAccessToken(user.getUsername(), user.getUserRole());
		String refreshToken = jwtUtil.createRefreshToken(user.getUsername());

		return UserSignInResponseDto.from(accessToken, refreshToken);
	}

	private void checkDeletedUser(User user) {
		if (user.isDeletedStatus()) {
			throw new CustomException(UserExceptionCode.DELETED_USER);
		}
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
