package io.notfound.counsel_back.security.jwt;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.SecurityException;
import io.jsonwebtoken.security.Keys;
import io.notfound.counsel_back.user.entity.UserRole;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

@Slf4j
@Component
public class JwtTokenProvider {

    private final SecretKey secretKey;
    private final long accessTokenValidTime;  // = accessTokenExpirationMinutes * 60 * 1000L;
    private final long refreshTokenValidTime; // = refreshTokenExpirationDays * 24 * 60 * 60 * 1000L;
    private final UserDetailsService userDetailsService;

    public JwtTokenProvider(
            @Value("${jwt.secret}") String secret,
            @Value("${jwt.access-token-expiration-minutes}") int accessTokenExpirationMinutes,
            @Value("${jwt.refresh-token-expiration-days}") int refreshTokenExpirationDays,
            UserDetailsService userDetailsService) {

        this.secretKey = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
        this.accessTokenValidTime = accessTokenExpirationMinutes * 60 * 1000L;
        this.refreshTokenValidTime = refreshTokenExpirationDays * 24 * 60 * 60 * 1000L;
        this.userDetailsService =  userDetailsService;
    }

    public boolean validateAccessToken(String jwtToken) {
        try {
            Jwts.parser()
                    .verifyWith(secretKey)
                    .build()
                    .parseSignedClaims(jwtToken);
            return true;

        } catch (ExpiredJwtException e) {
            log.info("Token expired: {}", e.getMessage());
            return false;
        } catch (JwtException e) {
            log.warn("Invalid access token: {}", e.getMessage());
            return false;
        }
    }

    public void validateRefreshToken(String jwtToken) throws JwtException {
        if (jwtToken == null || jwtToken.trim().isEmpty()) {
            throw new JwtException("Token is empty ");
        }
        try {
            Jwts.parser()
                    .verifyWith(secretKey)
                    .build()
                    .parseSignedClaims(jwtToken);

        } catch (ExpiredJwtException e) {
            throw new JwtException("Token expired", e);
        } catch (UnsupportedJwtException | MalformedJwtException | SecurityException e) {
            throw new JwtException("Invalid token", e);
        }
    }

    public String createAccessToken(String subject, UserRole roles) {
        return createToken(subject, roles, accessTokenValidTime);
    }

    public String createRefreshToken(String subject, UserRole roles) {
        return createToken(subject, roles, refreshTokenValidTime);
    }

    private String createToken(String subject, UserRole roles, long validTime) {
        Date now = new Date();
        Date expiration = new Date(now.getTime() + validTime);

        return Jwts.builder()
                .subject(subject)
                .claim("roles", roles.name())
                .issuedAt(now)
                .expiration(expiration)
                .signWith(secretKey)
                .compact();
    }

    public String getUserId(String token) {
        try {
            return Jwts.parser()
                    .verifyWith(secretKey)
                    .build()
                    .parseSignedClaims(token)
                    .getPayload()
                    .getSubject();
        } catch (JwtException e) {
            return null;
        }
    }

    public Authentication getAuthentication(String token) {
        UserDetails userDetails = userDetailsService.loadUserByUsername(this.getUserId(token));
        if (userDetails == null) return null;
        return new UsernamePasswordAuthenticationToken(userDetails, "", userDetails.getAuthorities());
    }

}
