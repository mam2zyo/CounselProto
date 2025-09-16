package io.notfound.counsel_back.auth.service;

import io.notfound.counsel_back.security.core.CustomOAuth2User;
import io.notfound.counsel_back.user.entity.User;
import io.notfound.counsel_back.user.entity.UserRole;
import io.notfound.counsel_back.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;

/**
 * OAuth2 사용자 정보를 처리하는 서비스
 * Google, Naver OAuth2 로그인 지원
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class OAuth2UserService extends DefaultOAuth2UserService {

    private final UserRepository userRepository;

    @Override
    @Transactional
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
        log.info("OAuth2 사용자 정보 로드 시작");

        // 1. 기본 OAuth2User 정보 가져오기
        OAuth2User oAuth2User = super.loadUser(userRequest);
        String registrationId = userRequest.getClientRegistration().getRegistrationId();

        log.info("OAuth2 Provider: {}", registrationId);

        // 2. Provider별 사용자 정보 추출
        UserInfo userInfo = extractUserInfo(registrationId, oAuth2User);

        if (userInfo.email == null) {
            log.error("이메일 정보를 가져올 수 없습니다. Provider: {}", registrationId);
            throw new OAuth2AuthenticationException("이메일 정보가 필요합니다.");
        }

        // 3. DB에서 사용자 조회 또는 생성
        User user = userRepository.findByEmail(userInfo.email)
                .orElseGet(() -> {
                    log.info("새 OAuth2 사용자 생성: {}", userInfo.email);
                    return createNewUser(userInfo, registrationId);
                });

        log.info("OAuth2 사용자 정보 로드 완료: {}", user.getEmail());
        return new CustomOAuth2User(user, oAuth2User.getAttributes(), registrationId);
    }

    /**
     * Provider별 사용자 정보 추출
     */
    private UserInfo extractUserInfo(String registrationId, OAuth2User oAuth2User) {
        switch (registrationId.toLowerCase()) {
            case "google":
                return extractGoogleUserInfo(oAuth2User);
            case "naver":
                return extractNaverUserInfo(oAuth2User);
            default:
                log.warn("지원하지 않는 OAuth2 Provider: {}", registrationId);
                return new UserInfo(null, null);
        }
    }

    /**
     * Google 사용자 정보 추출
     */
    private UserInfo extractGoogleUserInfo(OAuth2User oAuth2User) {
        String email = oAuth2User.getAttribute("email");
        String name = oAuth2User.getAttribute("name");

        // name이 없으면 email을 사용
        if (name == null || name.trim().isEmpty()) {
            name = email != null ? email.split("@")[0] : "Google User";
        }

        log.debug("Google 사용자 정보 - email: {}, name: {}", email, name);
        return new UserInfo(email, name);
    }

    /**
     * Naver 사용자 정보 추출
     */
    private UserInfo extractNaverUserInfo(OAuth2User oAuth2User) {
        Map<String, Object> response = oAuth2User.getAttribute("response");

        if (response == null) {
            log.warn("Naver response가 null입니다.");
            return new UserInfo(null, null);
        }

        String email = (String) response.get("email");
        String name = (String) response.get("name");

        // name이 없으면 nickname을 시도
        if (name == null || name.trim().isEmpty()) {
            name = (String) response.get("nickname");
        }

        // 그래도 없으면 email 사용
        if (name == null || name.trim().isEmpty()) {
            name = email != null ? email.split("@")[0] : "Naver User";
        }

        log.debug("Naver 사용자 정보 - email: {}, name: {}", email, name);
        return new UserInfo(email, name);
    }

    /**
     * 새 사용자 생성
     */
    private User createNewUser(UserInfo userInfo, String registrationId) {
        User newUser = User.builder()
                .email(userInfo.email)
                .name(userInfo.name)  // User 엔티티에 name 필드가 있어야 함
                .role(UserRole.USER)
                // .loginType(LoginType.valueOf(registrationId.toUpperCase())) // 필요시 추가
                // .password(null) // OAuth2 사용자는 비밀번호 없음
                .build();

        return userRepository.save(newUser);
    }

    /**
     * 사용자 정보를 담는 내부 클래스
     */
    private static class UserInfo {
        final String email;
        final String name;

        UserInfo(String email, String name) {
            this.email = email;
            this.name = name;
        }
    }
}