package com.live_commerce.user.domain.model;

import java.util.UUID;

import org.hibernate.annotations.UuidGenerator;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "p_user")
public class User extends BaseEntity {
	@Id
	@UuidGenerator
	private UUID userId;

	@Column(nullable = false, unique = true)
	private String username; // 사용자 ID

	@Column(nullable = false)
	private String password; // 사용자 비밀번호

	@Column(nullable = false, unique = true)
	private String email; // 사용자 이메일

	@Column(nullable = false)
	private String nickname; // 사용자명

	@Column(nullable = false)
	private boolean alarmConsent; // 알림 수신 동의 여부

	@Enumerated(EnumType.STRING)
	@Column(nullable = false, length = 30)
	private UserRole userRole; // 사용자 역할

	@Column(nullable = false)
	private boolean approved;

	// 정적 팩토리 메서드
	public static User of(String username, String password, String email, String nickname,
		boolean alarmConsent, UserRole userRole, boolean approved) {
		return new User(username, password, email, nickname, alarmConsent, userRole, approved);
	}

	// 프라이빗 생성자
	private User(String username, String password, String email, String nickname,
		boolean alarmConsent, UserRole userRole, boolean approved) {
		this.username = username;
		this.password = password;
		this.email = email;
		this.nickname = nickname;
		this.alarmConsent = alarmConsent;
		this.userRole = userRole;
		this.approved = approved;
	}

	public void updateUser(String password, String email, String nickname, boolean alarmConsent, UserRole userRole) {
		this.password = password;
		this.email = email;
		this.nickname = nickname;
		this.alarmConsent = alarmConsent;
		this.userRole = userRole;
	}

	public void changePassword(String newEncodedPassword) {
		this.password = newEncodedPassword;
	}

	public boolean isApproved() {
		return approved;
	}

	public void approve() {
		this.approved = true;
	}


}

