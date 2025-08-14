package io.notfound.counsel_back.security.jwt;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.SecurityException;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
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

    public JwtTokenProvider(
            @Value("${jwt.secret}") String secret,
            @Value("${jwt.access-token-expiration-minutes}") int accessTokenExpirationMinutes,
            @Value("${jwt.refresh-token-expiration-days}") int refreshTokenExpirationDays) {

        this.secretKey = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
        this.accessTokenValidTime = accessTokenExpirationMinutes * 60 * 1000L;
        this.refreshTokenValidTime = refreshTokenExpirationDays * 24 * 60 * 60 * 1000L;
    }

    public void validateToken(String jwtToken) throws JwtException {
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





//    public String validateRefreshToken(RefreshToken refreshToken) {
//        String token = refreshToken.getToken();
//
//        try {
//            Jws<Claims> claims = Jwts.parser()
//                    .verifyWith(secretKey)      // setSigningKey -> verifyWith
//                    .build()
//                    .parseSignedClaims(token);  // parseClaimsJws -> parseSignedClaims
//
//            Claims body = claims.getPayload();  // getBody() -> getPayload()
//            if (!body.getExpiration().before(new Date())) {
//                return recreationAccessToken(body.getSubject(), body.get("roles"));
//            }
//        } catch (JwtException e) {
//            return null;
//        }
//        return null;
//    }



//    public String recreationAccessToken(String userEmail, Object roles) {
//        return createAccessToken(userEmail, (UserRole) roles);
//    }
//
//    public Authentication getAuthentication(String token) {
//        UserDetails userDetails = userDetailService.loadUserByUsername(this.getUserPk(token));
//        if (userDetails == null) return null;
//        return new UsernamePasswordAuthenticationToken(userDetails, "", userDetails.getAuthorities());
//    }

    public String getUserPk(String token) {
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


//    private String createAccessToken(String subject, UserRole roles) {
//        Date now = new Date();
//        Date expiration = new Date(now.getTime() + accessTokenValidTime);
//
//        return Jwts.builder()
//                .subject(subject)                   // setSubject -> subject
//                .claim("roles", roles)        // setClaims + claims.put -> claim
//                .issuedAt(now)                      // setIssuedAt -> issuedAt
//                .expiration(expiration)             // setExpiration -> expiration
//                .signWith(secretKey)                // signWith(SignatureAlgorithm.HS256, key) -> signWith(key)
//                .compact();
//    }
//
//    private String createRefreshToken(String subject, UserRole roles) {
//        Date now = new Date();
//        Date expiration = new Date(now.getTime() + refreshTokenValidTime);
//
//        return Jwts.builder()
//                .subject(subject)
//                .claim("roles", roles)
//                .issuedAt(now)
//                .expiration(expiration)
//                .signWith(secretKey)
//                .compact();
//    }
}
