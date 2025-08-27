package io.notfound.counsel_back.security.handler;

import io.notfound.counsel_back.security.service.TokenService;
import io.notfound.counsel_back.user.dto.CustomOAuth2User;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;
import org.springframework.web.util.UriComponentsBuilder;

import java.io.IOException;

/**
 * Handles successful OAuth2 logins. Generates JWT and Refresh tokens and redirects the user to the frontend.
 * OAuth2 로그인 성공 시 호출되는 핸들러입니다. JWT 및 Refresh 토큰을 생성하여 사용자에게 전달합니다.
 */
@Component
@RequiredArgsConstructor
public class OAuth2LoginSuccessHandler implements AuthenticationSuccessHandler {

    private final TokenService tokenService;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication) throws IOException, ServletException {
        // 1. Get the authenticated user principal.
        //    인증된 사용자 정보를 가져옵니다.
        CustomOAuth2User principal = (CustomOAuth2User) authentication.getPrincipal();
        String email = principal.getUser().getEmail();

        // 2. Generate access token and refresh token.
        //    액세스 토큰과 리프레시 토큰을 생성합니다.
        String accessToken = tokenService.generateAccessToken(email);
        String refreshToken = tokenService.generateRefreshToken(email);

        // 3. Save the refresh token to the database (for later reissuance).
        //    리프레시 토큰을 DB에 저장합니다. (재발급을 위함)
        tokenService.saveRefreshToken(email, refreshToken);

        // 4. Build the redirect URI with tokens as query parameters.
        //    토큰을 쿼리 파라미터로 포함하여 리디렉션 URI를 생성합니다.
        String redirectUri = UriComponentsBuilder.fromUriString("http://localhost:3000/oauth/callback")
                .queryParam("accessToken", accessToken)
                .queryParam("refreshToken", refreshToken)
                .build().toUriString();

        // 5. Redirect the user to the frontend with the tokens.
        //    토큰을 담아 프론트엔드로 리디렉션합니다.
        response.sendRedirect(redirectUri);
    }
}