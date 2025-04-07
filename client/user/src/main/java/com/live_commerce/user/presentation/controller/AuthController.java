package com.live_commerce.user.presentation.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.live_commerce.user.application.dto.auth.request.UserFindUsernameRequestDto;
import com.live_commerce.user.application.dto.auth.request.UserFindUsernameVerifyRequestDto;
import com.live_commerce.user.application.dto.auth.request.UserSignInRequestDto;
import com.live_commerce.user.application.dto.auth.request.UserSignUpRequestDto;
import com.live_commerce.user.application.dto.auth.response.UserSignInResponseDto;
import com.live_commerce.user.application.dto.auth.response.UserSignUpResponseDto;
import com.live_commerce.user.application.service.AuthService;
import com.live_commerce.user.application.service.MailService;
import com.live_commerce.user.infrastructure.common.ResponseUtil;
import com.live_commerce.user.presentation.common.ApiResponse;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthController {

	private final AuthService authService;
	private final MailService mailService;

	@PostMapping("/signUp")
	public ResponseEntity<ApiResponse<UserSignUpResponseDto>> signUp(
		@RequestBody @Valid UserSignUpRequestDto requestDto
	) {
		UserSignUpResponseDto response = authService.signUp(requestDto);

		return ResponseUtil.success(response);
	}

	@PostMapping("/signIn")
	public ResponseEntity<ApiResponse<UserSignInResponseDto>> signIn(@RequestBody UserSignInRequestDto requestDto) {
		UserSignInResponseDto response = authService.signIn(requestDto);

		return ResponseUtil.success(response);
	}

	@PostMapping("/find-username/send")
	public ResponseEntity<ApiResponse<String>> sendFindUsernameCode(
		@RequestBody @Valid UserFindUsernameRequestDto request
	) {
		authService.sendFindUsernameCode(request.email());
		return ResponseUtil.success("인증번호가 이메일로 전송되었습니다.");
	}

	@PostMapping("/find-username/verify")
	public ResponseEntity<ApiResponse<String>> confirmFindUsernameCode(
		@RequestBody @Valid UserFindUsernameVerifyRequestDto request
	) {
		String username = authService.confirmFindUsernameCode(request.email(), request.code());
		return ResponseUtil.success(username);
	}


}
