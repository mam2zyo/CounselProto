package io.notfound.counsel_back.user.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@Entity // JPA 엔티티로 지정
@Table(name = "users") // 테이블 이름 지정 (user는 예약어일 수 있어 users로 사용)
@Getter // Lombok: 모든 필드의 Getter 메서드 자동 생성
@NoArgsConstructor(access = AccessLevel.PROTECTED) // Lombok: 기본 생성자 자동 생성
@EntityListeners(AuditingEntityListener.class) // 생성/수정 시간 자동 기록
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String email;

    private String name; // 이름 필드 추가

    private String password;

    @Enumerated(EnumType.STRING) // Enum 타입을 DB에 문자열로 저장
    private UserRole role;

    @CreatedDate
    @Column(updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    private LocalDateTime updatedAt;

    // Refresh Token 필드 추가
    @Column(length = 500) // 토큰 길이를 넉넉하게 설정
    private String refreshToken;

    @Builder
    public User(String email, String name, String password, UserRole role) {
        this.email = email;
        this.name = name;
        this.password = password;
        this.role = role;
    }

    /**
     * Refreshes the refresh token.
     * 리프레시 토큰을 업데이트합니다.
     *
     * @param refreshToken new refresh token 새로운 리프레시 토큰
     */
    public void updateRefreshToken(String refreshToken) {
        this.refreshToken = refreshToken;
    }
}
