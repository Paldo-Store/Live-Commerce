package com.live_commerce.user.presentation.controller;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.live_commerce.user.application.dto.auth.request.UserSearchCondition;
import com.live_commerce.user.application.dto.auth.request.UserUpdateRequestDto;
import com.live_commerce.user.application.dto.auth.response.UserUpdateResponseDto;
import com.live_commerce.user.application.dto.user.response.UserGetResponseDto;
import com.live_commerce.user.application.service.UserService;
import com.live_commerce.user.infrastructure.common.ResponseUtil;
import com.live_commerce.user.infrastructure.security.RequestUserDetails;
import com.live_commerce.user.presentation.common.ApiResponse;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
public class UserController {

	private final UserService userService;

	@GetMapping("/{username}")
	@PreAuthorize("#username == authentication.principal.username or hasRole('MASTER')")
	public ResponseEntity<ApiResponse<UserGetResponseDto>> getUser(
		@PathVariable String username,
		@AuthenticationPrincipal RequestUserDetails requestUserDetails
	) {
		UserGetResponseDto response = userService.getUser(username, requestUserDetails);
		return ResponseUtil.success(response);
	}

	@GetMapping("/search")
	@PreAuthorize("hasRole('MASTER')")
	public ResponseEntity<ApiResponse<Page<UserGetResponseDto>>> searchUsers(
		@ModelAttribute UserSearchCondition condition,
		@PageableDefault(size = 10) Pageable pageable,
		@AuthenticationPrincipal RequestUserDetails requestUserDetails
	) {
		Page<UserGetResponseDto> response = userService.searchUser(condition, pageable, requestUserDetails);

		return ResponseUtil.success(response);
	}

	@PutMapping("/{username}")
	@PreAuthorize("#username == authentication.principal.username or hasRole('MASTER')")
	public ResponseEntity<ApiResponse<UserUpdateResponseDto>> updateUser(
		@PathVariable String username,
		@RequestBody @Valid UserUpdateRequestDto requestDto,
		@AuthenticationPrincipal RequestUserDetails requestUserDetails
	) {
		UserUpdateResponseDto response = userService.updateUser(username, requestDto, requestUserDetails);

		return ResponseUtil.success(response);
	}

	@DeleteMapping("/{username}")
	@PreAuthorize("hasRole('MASTER')")
	public ResponseEntity<ApiResponse<Void>> deleteUser(
		@PathVariable String username,
		@AuthenticationPrincipal RequestUserDetails requestUserDetails
	) {
		userService.deleteUser(username, requestUserDetails);

		return ResponseUtil.noContent();
	}
}
