package com.live_commerce.user.presentation.controller;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.live_commerce.user.application.dto.auth.request.UserSearchCondition;
import com.live_commerce.user.application.dto.user.response.UserGetResponseDto;
import com.live_commerce.user.application.service.UserService;
import com.live_commerce.user.infrastructure.common.ResponseUtil;
import com.live_commerce.user.infrastructure.security.RequestUserDetails;
import com.live_commerce.user.presentation.common.ApiResponse;

import lombok.RequiredArgsConstructor;

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

	// 관리자용 전체 유저 조회
	@GetMapping("/search")
	@PreAuthorize("hasRole('MASTER')")
	public ResponseEntity<ApiResponse<Page<UserGetResponseDto>>> searchUsers(
		@ModelAttribute UserSearchCondition condition,
		@PageableDefault(size = 10) Pageable pageable
	) {
		Page<UserGetResponseDto> response = userService.searchUser(condition, pageable);

		return ResponseUtil.success(response);
	}

}
