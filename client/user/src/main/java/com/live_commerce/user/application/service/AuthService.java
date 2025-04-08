package com.live_commerce.user.application.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.live_commerce.user.application.dto.auth.request.UserSignInRequestDto;
import com.live_commerce.user.application.dto.auth.request.UserSignUpRequestDto;
import com.live_commerce.user.application.dto.auth.response.UserSignInResponseDto;
import com.live_commerce.user.application.dto.auth.response.UserSignUpResponseDto;
import com.live_commerce.user.application.dto.auth.response.TokenReissueResponseDto;
import com.live_commerce.user.application.exception.CustomException;
import com.live_commerce.user.application.exception.UserExceptionCode;
import com.live_commerce.user.domain.model.User;
import com.live_commerce.user.domain.repository.UserRepository;
import com.live_commerce.user.infrastructure.common.JwtUtil;
import com.live_commerce.user.infrastructure.common.PasswordGenerator;
import com.live_commerce.user.infrastructure.common.RedisUtil;

import io.jsonwebtoken.Claims;
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

	private static final String REFRESH_KEY_PREFIX = "RT:";

	@Value("${service.jwt.refresh-expiration}")
	private long refreshTokenExpirationMillis;

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
			.map(this::validateActiveUser)
			.orElseThrow(() -> new CustomException(UserExceptionCode.INVALID_CREDENTIALS));

		String accessToken = jwtUtil.createAccessToken(user.getUsername(), user.getUserRole());
		String refreshToken = jwtUtil.createRefreshToken(user.getUsername());

		String redisKey = REFRESH_KEY_PREFIX + user.getUsername();
		redisUtil.setDataExpire(redisKey, refreshToken, refreshTokenExpirationMillis);

		return UserSignInResponseDto.from(accessToken, refreshToken);
	}

	@Transactional
	public TokenReissueResponseDto reissueToken(String refreshToken) {
		jwtUtil.validateToken(refreshToken);
		Claims token = jwtUtil.parseClaims(refreshToken);

		String username = token.get("username", String.class);

		String redisKey = REFRESH_KEY_PREFIX + username;
		String storedRefreshToken = redisUtil.getData(redisKey);

		if (storedRefreshToken == null || !storedRefreshToken.equals(refreshToken)) {
			throw new CustomException(UserExceptionCode.INVALID_REFRESH_TOKEN);
		}

		User user = userRepository.findByUsername(username)
			.map(this::validateActiveUser)
			.orElseThrow(() -> new CustomException(UserExceptionCode.USER_NOT_FOUND));

		String newAccessToken = jwtUtil.createAccessToken(username, user.getUserRole());
		String newRefreshToken = jwtUtil.createRefreshToken(username);

		redisUtil.setDataExpire(redisKey, newRefreshToken, refreshTokenExpirationMillis);

		return new TokenReissueResponseDto(newAccessToken, newRefreshToken);
	}

	@Transactional
	public void logout(String username) {
		String redisKey = REFRESH_KEY_PREFIX + username;
		redisUtil.deleteData(redisKey);
	}

	@Transactional
	public void sendFindUsernameCode(String email) {
		findActiveUserByEmail(email);
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

		User user = findActiveUserByEmail(email);
		redisUtil.deleteData(email);
		return user.getUsername();
	}

	@Transactional
	public void resetPasswordAndSendTempPassword(String username, String email) {
		User user = findActiveUserByUsernameAndEmail(username, email);

		String tempPassword = PasswordGenerator.generateTempPassword(10);
		String encoded = passwordEncoder.encode(tempPassword);

		user.changePassword(encoded);
		mailService.sendTemporaryPassword(email, tempPassword);
	}

	// =================== 공통 로직 ===================

	private User findActiveUserByEmail(String email) {
		return userRepository.findByEmail(email)
			.map(this::validateActiveUser)
			.orElseThrow(() -> new CustomException(UserExceptionCode.USER_NOT_FOUND));
	}

	private User findActiveUserByUsernameAndEmail(String username, String email) {
		return userRepository.findByUsernameAndEmail(username, email)
			.map(this::validateActiveUser)
			.orElseThrow(() -> new CustomException(UserExceptionCode.USER_NOT_FOUND));
	}

	private User validateActiveUser(User user) {
		if (user.isDeletedStatus()) {
			throw new CustomException(UserExceptionCode.DELETED_USER);
		}
		return user;
	}

	private void validateUsername(String username) {
		validateDuplicate(userRepository.existsByUsername(username), UserExceptionCode.DUPLICATE_USERNAME);
	}

	private void validateEmail(String email) {
		validateDuplicate(userRepository.existsByEmail(email), UserExceptionCode.DUPLICATE_EMAIL);
	}

	private void validateDuplicate(boolean exists, UserExceptionCode exceptionCode) {
		if (exists) {
			throw new CustomException(exceptionCode);
		}
	}
}
