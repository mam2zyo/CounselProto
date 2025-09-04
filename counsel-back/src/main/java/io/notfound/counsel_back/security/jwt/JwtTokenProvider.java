package io.notfound.counsel_back.security.jwt;

import io.jsonwebtoken.*;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import io.jsonwebtoken.security.SignatureException;
import io.notfound.counsel_back.user.entity.UserRole;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.util.Date;

/**
 * JWT(Json Web Token)의 생성, 검증, 파싱을 담당하는 유틸리티 클래스입니다.
 * 보안 관련 핵심 로직이므로, Spring 빈으로 등록하여 관리합니다.
 */
@Slf4j // 로깅을 위한 Lombok 어노테이션
@Component // 이 클래스를 Spring 빈으로 등록합니다.
public class JwtTokenProvider {

    private final SecretKey secretKey;
    private final long accessTokenValidTime;
    private final long refreshTokenValidTime;
    private final UserDetailsService userDetailsService;

    /**
     * 의존성 주입을 통해 JWT 설정값을 초기화합니다.
     * @param secret application.properties에 정의된 JWT 비밀 키
     * @param accessTokenExpirationMinutes 액세스 토큰 만료 시간 (분 단위)
     * @param refreshTokenExpirationDays 리프레시 토큰 만료 시간 (일 단위)
     * @param userDetailsService Spring Security의 사용자 정보 로드 서비스
     */
    public JwtTokenProvider(
            @Value("${jwt.secret}") String secret,
            @Value("${jwt.access-token-expiration-minutes}") int accessTokenExpirationMinutes,
            @Value("${jwt.refresh-token-expiration-days}") int refreshTokenExpirationDays,
            UserDetailsService userDetailsService) {

        // 1. 비밀 키를 Base64 문자열에서 SecretKey 객체로 변환
        this.secretKey = Keys.hmacShaKeyFor(Decoders.BASE64.decode(secret));

        // 2. 만료 시간을 밀리초 단위로 계산
        this.accessTokenValidTime = (long) accessTokenExpirationMinutes * 60 * 1000L;
        this.refreshTokenValidTime = (long) refreshTokenExpirationDays * 24 * 60 * 60 * 1000L;

        this.userDetailsService = userDetailsService;
    }

    /**
     * JWT 토큰의 유효성을 검증하고, 유효하지 않은 경우 명확한 예외를 던집니다.
     * @param jwtToken 검증할 JWT 문자열
     * @return 토큰이 유효하면 true, 아니면 false 반환
     */
    public boolean validateToken(String jwtToken) {
        if (jwtToken == null || jwtToken.trim().isEmpty()) {
            log.warn("Token is empty or null.");
            return false;
        }
        try {
            Jwts.parser()
                    .verifyWith(secretKey)
                    .build()
                    .parseSignedClaims(jwtToken);
            return true;
        } catch (ExpiredJwtException e) {
            log.info("Token expired: {}", e.getMessage());
        } catch (UnsupportedJwtException | MalformedJwtException | SignatureException e) {
            log.warn("Invalid token: {}", e.getMessage());
        } catch (IllegalArgumentException e) {
            log.warn("JWT token is invalid: {}", e.getMessage());
        }
        return false;
    }

    /**
     * 액세스 토큰을 생성합니다.
     * @param subject 토큰의 주체 (예: 사용자 이메일)
     * @param roles 사용자 역할
     * @return 생성된 액세스 토큰 문자열
     */
    public String createAccessToken(String subject, UserRole roles) {
        return buildToken(subject, roles, accessTokenValidTime);
    }

    /**
     * 리프레시 토큰을 생성합니다.
     * @param subject 토큰의 주체 (예: 사용자 이메일)
     * @param roles 사용자 역할
     * @return 생성된 리프레시 토큰 문자열
     */
    public String createRefreshToken(String subject, UserRole roles) {
        return buildToken(subject, roles, refreshTokenValidTime);
    }

    /**
     * JWT 토큰을 실제로 생성하는 내부 메서드입니다.
     * @param subject 토큰의 주체
     * @param roles 사용자 역할
     * @param validTime 유효 시간 (밀리초)
     * @return 생성된 토큰 문자열
     */
    private String buildToken(String subject, UserRole roles, long validTime) {
        Date now = new Date();
        Date expiration = new Date(now.getTime() + validTime);

        return Jwts.builder()
                .subject(subject) // 토큰의 주체 (사용자 식별자)
                .claim("roles", roles.name()) // 사용자 역할 정보
                .issuedAt(now) // 토큰 발행 시간
                .expiration(expiration) // 토큰 만료 시간
                .signWith(secretKey) // 토큰에 서명
                .compact(); // 토큰을 문자열로 직렬화
    }

    /**
     * JWT에서 사용자 식별자(subject)를 추출합니다.
     * 유효하지 않은 토큰일 경우 예외를 던집니다.
     * @param token 파싱할 토큰 문자열
     * @return 사용자 식별자 (예: 이메일)
     * @throws JwtException 유효하지 않은 토큰일 경우
     */
    public String getUserId(String token) throws JwtException {
        return Jwts.parser()
                .verifyWith(secretKey)
                .build()
                .parseSignedClaims(token)
                .getPayload()
                .getSubject();
    }

    /**
     * 토큰으로부터 Authentication 객체를 생성합니다.
     * 이 객체는 Spring Security의 SecurityContext에 저장됩니다.
     * @param token 토큰 문자열
     * @return 인증 객체
     */
    public Authentication getAuthentication(String token) {
        // 토큰에서 추출한 사용자 ID로 UserDetails를 로드
        String userId = getUserId(token);
        UserDetails userDetails = userDetailsService.loadUserByUsername(userId);

        // UserDetails와 권한 정보를 바탕으로 Authentication 객체 생성
        return new UsernamePasswordAuthenticationToken(userDetails, "", userDetails.getAuthorities());
    }
}