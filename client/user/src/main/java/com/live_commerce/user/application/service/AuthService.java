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
import com.live_commerce.user.infrastructure.common.RedisUtil;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AuthService {

	private final UserRepository userRepository;
	private final PasswordEncoder passwordEncoder;
	private final JwtUtil jwtUtil;
	private final RedisUtil redisUtil;
	private final MailService mailService;

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

	@Transactional
	public void sendFindUsernameCode(String email) {
		User user = userRepository.findByEmail(email)
			.orElseThrow(() -> new CustomException(UserExceptionCode.USER_NOT_FOUND));
		checkDeletedUser(user);

		mailService.sendVerificationCode(email);
	}

	@Transactional
	public String confirmFindUsernameCode(String email, String inputCode) {
		String storedCode = redisUtil.getData(email);

		if (storedCode == null) {
			throw new CustomException(UserExceptionCode.VERIFICATION_CODE_EXPIRED);
		}

		if (!storedCode.equals(inputCode)) {
			throw new CustomException(UserExceptionCode.INVALID_VERIFICATION_CODE);
		}

		User user = userRepository.findByEmail(email)
			.orElseThrow(() -> new CustomException(UserExceptionCode.USER_NOT_FOUND));
		checkDeletedUser(user);

		redisUtil.deleteData(email);
		return user.getUsername();
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
