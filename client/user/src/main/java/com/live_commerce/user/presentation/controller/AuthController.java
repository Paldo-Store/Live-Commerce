package com.live_commerce.user.presentation.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.live_commerce.user.application.dto.auth.request.UserSignInRequestDto;
import com.live_commerce.user.application.dto.auth.request.UserSignUpRequestDto;
import com.live_commerce.user.application.dto.auth.response.UserSignInResponseDto;
import com.live_commerce.user.application.dto.auth.response.UserSignUpResponseDto;
import com.live_commerce.user.application.service.AuthService;
import com.live_commerce.user.infrastructure.common.ResponseUtil;
import com.live_commerce.user.presentation.common.ApiResponse;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthController {

	private final AuthService authService;

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

}
