package com.live_commerce.user.application.service;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.live_commerce.user.application.dto.auth.request.*;
import com.live_commerce.user.application.dto.auth.response.*;
import com.live_commerce.user.application.exception.*;
import com.live_commerce.user.domain.model.*;
import com.live_commerce.user.domain.repository.UserRepository;
import com.live_commerce.user.infrastructure.client.CouponClient;
import com.live_commerce.user.infrastructure.common.JwtUtil;
import com.live_commerce.user.infrastructure.common.RedisUtil;

import io.jsonwebtoken.Claims;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class AuthServiceTest {

	@Autowired private AuthService authService;

	@MockitoBean
	private UserRepository userRepository;
	@MockitoBean
	private PasswordEncoder passwordEncoder;
	@MockitoBean
	private JwtUtil jwtUtil;
	@MockitoBean
	private RedisUtil redisUtil;
	@MockitoBean
	private MailService mailService;
	@MockitoBean
	private CouponClient couponClient;

	@DisplayName("회원가입 성공")
	@Test
	void signUp_success() {
		// Given
		UserSignUpRequestDto req = new UserSignUpRequestDto("testuser", "password", "test@email.com", "nickname", true, UserRole.CUSTOMER, null);
		when(userRepository.existsByEmail(req.email())).thenReturn(false);
		when(passwordEncoder.encode(req.password())).thenReturn("encoded");
		when(userRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

		// When
		UserSignUpResponseDto res = authService.signUp(req);

		// Then
		assertThat(res).isNotNull();
		verify(couponClient).issueFirstCoupon(any());
	}

	@DisplayName("회원가입 실패 - 마스터키 불일치")
	@Test
	void signUp_masterKey_invalid() {
		// Given
		UserSignUpRequestDto req = new UserSignUpRequestDto("admin", "pass", "admin@email.com", "nickname", true, UserRole.MASTER, "wrong-key");
		when(userRepository.existsByEmail(any())).thenReturn(false);

		// When & Then
		assertThatThrownBy(() -> authService.signUp(req))
			.isInstanceOf(CustomException.class)
			.hasMessage(UserExceptionCode.INVALID_MASTER_KEY.getMessage());
	}

	@DisplayName("로그인 성공")
	@Test
	void signIn_success() {
		// Given
		User raw = User.of("testuser", "encoded", "email@test.com", "nickname", true, UserRole.CUSTOMER, false);
		User user = spy(raw);
		doReturn(true).when(user).isApproved();
		when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(user));
		when(passwordEncoder.matches("password", "encoded")).thenReturn(true);
		when(jwtUtil.createAccessToken(any(), any(), any())).thenReturn("access-token");
		when(jwtUtil.createRefreshToken(any())).thenReturn("refresh-token");

		UserSignInRequestDto req = new UserSignInRequestDto("testuser", "password");

		// When
		UserSignInResponseDto res = authService.signIn(req);

		// Then
		assertThat(res.accessToken()).isEqualTo("access-token");
		assertThat(res.refreshToken()).isEqualTo("refresh-token");
		verify(redisUtil).setDataExpire(startsWith("RT:"), eq("refresh-token"), anyLong());
	}

	@DisplayName("토큰 재발급 성공")
	@Test
	void reissueToken_success() {
		// Given
		UUID userId = UUID.randomUUID();
		Claims claims = mock(Claims.class);
		when(claims.get("userId", String.class)).thenReturn(userId.toString());
		when(jwtUtil.parseClaims("refresh-token")).thenReturn(claims);
		when(redisUtil.getData("RT:" + userId)).thenReturn("refresh-token");

		User user = User.of("testuser", "pw", "email", "nickname", true, UserRole.CUSTOMER, false);
		when(userRepository.findById(userId)).thenReturn(Optional.of(user));
		when(jwtUtil.createAccessToken(any(), any(), any())).thenReturn("new-access");
		when(jwtUtil.createRefreshToken(any())).thenReturn("new-refresh");

		// When
		TokenReissueResponseDto res = authService.reissueToken("refresh-token");

		// Then
		assertThat(res.accessToken()).isEqualTo("new-access");
		assertThat(res.refreshToken()).isEqualTo("new-refresh");
	}

	@DisplayName("로그아웃 성공")
	@Test
	void logout_success() {
		// Given
		UUID userId = UUID.randomUUID();

		// When
		authService.logout(userId);

		// Then
		verify(redisUtil).deleteData("RT:" + userId);
	}

	@DisplayName("임시 비밀번호 전송 성공")
	@Test
	void resetPasswordAndSendTempPassword_success() {
		// Given
		User user = User.of("user", "pw", "email", "nickname", true, UserRole.CUSTOMER, false);
		when(userRepository.findByUsernameAndEmail("user", "email")).thenReturn(Optional.of(user));
		when(passwordEncoder.encode(any())).thenReturn("encoded");

		// When
		authService.resetPasswordAndSendTempPassword("user", "email");

		// Then
		verify(userRepository).findByUsernameAndEmail("user", "email");
		verify(mailService).sendTemporaryPassword(eq("email"), any());
	}
}
