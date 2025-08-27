package io.notfound.counsel_back.security.service;

import io.notfound.counsel_back.user.entity.User;
import io.notfound.counsel_back.user.repository.UserRepository;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.Optional;

/**
 * Service for generating, validating, and managing JWT and Refresh tokens.
 * JWT 및 Refresh 토큰을 생성, 검증, 관리하는 서비스입니다.
 */
@Service
@RequiredArgsConstructor
public class TokenService {

    @Value("${jwt.secret}")
    private String jwtSecret;
    @Value("${jwt.access-token-expiration-minutes}")
    private long accessTokenExpirationMinutes;
    @Value("${jwt.refresh-token-expiration-days}")
    private long refreshTokenExpirationDays;

    private final UserRepository userRepository;

    /**
     * Generates a new JWT access token for the given user email.
     * 주어진 이메일에 대한 새로운 JWT 액세스 토큰을 생성합니다.
     *
     * @param email user's email 사용자 이메일
     * @return generated JWT string 생성된 JWT 문자열
     */
    public String generateAccessToken(String email) {
        SecretKey key = Keys.hmacShaKeyFor(jwtSecret.getBytes(StandardCharsets.UTF_8));
        Date now = new Date();
        Date expirationDate = new Date(now.getTime() + accessTokenExpirationMinutes * 60 * 1000);

        return Jwts.builder()
                .setSubject(email)
                .setIssuedAt(now)
                .setExpiration(expirationDate)
                .signWith(key)
                .compact();
    }

    /**
     * Generates a new Refresh token for the given user email.
     * 주어진 이메일에 대한 새로운 Refresh 토큰을 생성합니다.
     *
     * @param email user's email 사용자 이메일
     * @return generated Refresh token string 생성된 Refresh 토큰 문자열
     */
    public String generateRefreshToken(String email) {
        SecretKey key = Keys.hmacShaKeyFor(jwtSecret.getBytes(StandardCharsets.UTF_8));
        Date now = new Date();
        Date expirationDate = new Date(now.getTime() + refreshTokenExpirationDays * 24 * 60 * 60 * 1000);

        return Jwts.builder()
                .setSubject(email)
                .setIssuedAt(now)
                .setExpiration(expirationDate)
                .signWith(key)
                .compact();
    }

    /**
     * Saves the refresh token to the database, associating it with the user.
     * 리프레시 토큰을 DB에 저장합니다.
     *
     * @param email user's email 사용자 이메일
     * @param refreshToken refresh token to be saved 저장할 리프레시 토큰
     */
    @Transactional
    public void saveRefreshToken(String email, String refreshToken) {
        Optional<User> userOptional = userRepository.findByEmail(email);
        userOptional.ifPresent(user -> {
            user.updateRefreshToken(refreshToken);
            userRepository.save(user);
        });
    }

    // You can add more methods here like validateToken, reissueToken, etc.
}