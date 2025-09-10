package io.notfound.counsel_back.security.service;

import io.notfound.counsel_back.common.exception.CustomException;
import io.notfound.counsel_back.common.exception.ErrorCode;
import io.notfound.counsel_back.security.dto.LoginRequestDto;
import io.notfound.counsel_back.security.dto.LoginResponseDto;
import io.notfound.counsel_back.security.dto.RefreshTokenRequestDto;
import io.notfound.counsel_back.security.jwt.JwtTokenProvider;
import io.notfound.counsel_back.security.entity.RefreshToken;
import io.notfound.counsel_back.security.repository.RefreshTokenRepository;
import io.notfound.counsel_back.user.entity.User;
import io.notfound.counsel_back.user.entity.UserRole;
import io.notfound.counsel_back.user.repository.UserRepository;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

/**
 * 사용자 인증(Authentication)과 관련된 비즈니스 로직을 처리하는 서비스 클래스입니다.
 * 회원가입, 로그인, 토큰 갱신, 로그아웃 기능을 담당합니다.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AuthService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;
    private final AuthenticationManagerBuilder authenticationManagerBuilder;
    private final RefreshTokenRepository refreshTokenRepository;

    /**
     * 새로운 사용자를 등록하는 회원가입 메서드입니다.
     */
    @Transactional
    public User signup(String email, String password) {
        if (userRepository.findByEmail(email).isPresent()) {
            throw new CustomException(ErrorCode.USER_ALREADY_EXISTS);
        }
        User user = User.builder()
                .email(email)
                .password(passwordEncoder.encode(password))
                .role(UserRole.USER)
                .build();
        return userRepository.save(user);
    }

    /**
     * 사용자의 로그인 정보를 검증하고 JWT 토큰을 발급합니다.
     * 로그인 성공 시 리프레시 토큰을 데이터베이스에 저장합니다.
     */
    @Transactional
    public LoginResponseDto login(LoginRequestDto loginRequestDto) {
        UsernamePasswordAuthenticationToken authenticationToken =
                new UsernamePasswordAuthenticationToken(loginRequestDto.getEmail(), loginRequestDto.getPassword());

        Authentication authentication = authenticationManagerBuilder.getObject()
                .authenticate(authenticationToken);

        String email = authentication.getName();
        String accessToken = jwtTokenProvider.createAccessToken(email, UserRole.USER);
        String refreshToken = jwtTokenProvider.createRefreshToken(email, UserRole.USER);

        // 기존 리프레시 토큰이 있다면 삭제하고, 새로운 리프레시 토큰 저장
        Optional<RefreshToken> existingToken = refreshTokenRepository.findById(email);
        existingToken.ifPresent(refreshTokenRepository::delete);

        RefreshToken newRefreshToken = new RefreshToken(email, refreshToken);
        refreshTokenRepository.save(newRefreshToken);

        return LoginResponseDto.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .build();
    }

    /**
     * 유효한 리프레시 토큰을 사용하여 새로운 액세스 토큰을 발급합니다.
     */
    @Transactional
    public String refreshAccessToken(RefreshTokenRequestDto refreshTokenRequestDto) {
        String refreshToken = refreshTokenRequestDto.getRefreshToken();

        // 1. 리프레시 토큰의 유효성을 검증합니다.
        jwtTokenProvider.validateToken(refreshToken);

        // 2. 리프레시 토큰에서 사용자 ID(이메일)를 추출합니다.
        String email = jwtTokenProvider.getUserId(refreshToken);
        if (email == null) {
            throw new CustomException(ErrorCode.INVALID_TOKEN);
        }

        // 3. DB에 저장된 리프레시 토큰과 일치하는지 확인합니다.
        RefreshToken storedToken = refreshTokenRepository.findById(email)
                .orElseThrow(() -> new CustomException(ErrorCode.REFRESH_TOKEN_NOT_FOUND));

        if (!storedToken.getRefreshToken().equals(refreshToken)) {
            throw new CustomException(ErrorCode.INVALID_TOKEN);
        }

        // 4. 조회한 사용자 정보로 새로운 액세스 토큰을 생성하여 반환합니다.
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));

        return jwtTokenProvider.createAccessToken(user.getEmail(), user.getRole());
    }

    /**
     * 로그아웃 처리 시 데이터베이스에서 리프레시 토큰을 삭제합니다.
     */
    @Transactional
    public void logout(String email) {
        refreshTokenRepository.findById(email).ifPresent(refreshTokenRepository::delete);
    }
}
