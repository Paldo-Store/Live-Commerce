package com.live_commerce.user.presentation.controller;

import com.live_commerce.user.application.dto.user.response.UserGetResponseDto;
import com.live_commerce.user.application.service.UserService;
import com.live_commerce.user.infrastructure.common.ResponseUtil;
import com.live_commerce.user.infrastructure.security.RequestUserDetails;
import com.live_commerce.user.presentation.common.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
public class UserController {

	private final UserService userService;

	// 본인 정보 조회 (/me)
	@GetMapping("/me")
	public ResponseEntity<ApiResponse<UserGetResponseDto>> getMyInfo(
		@AuthenticationPrincipal RequestUserDetails requestUserDetails
	) {
		UserGetResponseDto response = userService.getUser(requestUserDetails.getUsername());

		return ResponseUtil.success(response);
	}

	// 관리자용 특정 유저 조회 (/users/{username})
	@GetMapping("/{username}")
	@PreAuthorize("hasRole('MASTER')")
	public ResponseEntity<ApiResponse<UserGetResponseDto>> getUserByAdmin(@PathVariable String username) {
		UserGetResponseDto response = userService.getUser(username);

		return ResponseUtil.success(response);
	}
}
