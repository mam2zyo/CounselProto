package io.notfound.counsel_back.common.util;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.ResponseCookie;

public class CookieUtil {

    private static final String ACCESS_TOKEN = "accessToken";
    private static final String REFRESH_TOKEN = "refreshToken";

    /**
     * 쿠키 생성 (ResponseCookie 사용)
     */
    private static void addCookie(HttpServletResponse response, String name, String value, int maxAge, boolean httpOnly) {
        ResponseCookie cookie = ResponseCookie.from(name, value)
                .httpOnly(httpOnly)
                .secure(true)          // HTTPS 환경에서만 전송
                .path("/")
                .maxAge(maxAge)
                .sameSite("None")      // 크로스 도메인 쿠키 허용
                .build();

        response.addHeader("Set-Cookie", cookie.toString());
    }

    /** AccessToken 쿠키 추가 */
    public static void addAccessToken(HttpServletResponse response, String token) {
        addCookie(response, ACCESS_TOKEN, token, 30 * 60, true); // 30분
    }

    /** RefreshToken 쿠키 추가 */
    public static void addRefreshToken(HttpServletResponse response, String token) {
        addCookie(response, REFRESH_TOKEN, token, 14 * 24 * 60 * 60, true); // 14일
    }

    /** 쿠키 삭제 */
    private static void deleteCookie(HttpServletResponse response, String name) {
        ResponseCookie cookie = ResponseCookie.from(name, "")
                .httpOnly(true)
                .secure(true)
                .path("/")
                .maxAge(0)
                .sameSite("None")
                .build();
        response.addHeader("Set-Cookie", cookie.toString());
    }

    /** AccessToken + RefreshToken 삭제 */
    public static void deleteTokens(HttpServletResponse response) {
        deleteCookie(response, ACCESS_TOKEN);
        deleteCookie(response, REFRESH_TOKEN);
    }

    /**
     * 요청에서 특정 쿠키 값을 추출하는 헬퍼 메서드
     */
    private static String getCookieValue(HttpServletRequest request, String cookieName) {
        if (request.getCookies() != null) {
            for (Cookie cookie : request.getCookies()) {
                if (cookieName.equals(cookie.getName())) {
                    return cookie.getValue();
                }
            }
        }
        return null;
    }

    /**
     * 요청에서 Access Token 추출
     */
    public static String getAccessToken(HttpServletRequest request) {
        return getCookieValue(request, ACCESS_TOKEN);
    }

    /**
     * 요청에서 Refresh Token 추출
     */
    public static String getRefreshToken(HttpServletRequest request) {
        return getCookieValue(request, REFRESH_TOKEN);
    }
}