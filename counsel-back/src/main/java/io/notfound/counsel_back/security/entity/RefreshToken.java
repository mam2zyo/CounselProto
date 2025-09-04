package io.notfound.counsel_back.security.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Getter
@NoArgsConstructor
@ToString
@Entity
@Table(name = "refresh_token")
public class RefreshToken {

    @Id
    @Column(name = "user_email", unique = true, nullable = false)
    private String userEmail;

    @Column(name = "refresh_token", nullable = false, length = 500) // 토큰 길이를 넉넉하게 설정
    private String refreshToken;

    public RefreshToken(String userEmail, String refreshToken) {
        this.userEmail = userEmail;
        this.refreshToken = refreshToken;
    }
}