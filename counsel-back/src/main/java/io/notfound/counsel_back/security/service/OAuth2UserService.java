package io.notfound.counsel_back.security.service;

import io.notfound.counsel_back.user.dto.CustomOAuth2User;
import io.notfound.counsel_back.user.entity.User;
import io.notfound.counsel_back.user.entity.UserRole;
import io.notfound.counsel_back.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;

/**
 * Spring Security의 DefaultOAuth2UserService를 상속받아 OAuth2 인증 후 사용자 정보를 처리합니다.
 * 구글, 네이버 등 각 Provider로부터 받은 사용자 정보를 파싱하여 우리 서비스의 User 엔티티로 변환합니다.
 */

@Service
@RequiredArgsConstructor
public class OAuth2UserService extends DefaultOAuth2UserService {

    private final UserRepository userRepository;

    @Override
    @Transactional
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
        OAuth2User oAuth2User = super.loadUser(userRequest);
        String registrationId = userRequest.getClientRegistration().getRegistrationId();

        final String email;
        final String name;

        if ("google".equals(registrationId)) {
            email = oAuth2User.getAttribute("email");
            String tempName = oAuth2User.getAttribute("name");
            name = (tempName != null) ? tempName : email;
        } else if ("naver".equals(registrationId)) {
            Map<String, Object> naverAttributes = oAuth2User.getAttribute("response");
            if (naverAttributes != null) {
                email = (String) naverAttributes.get("email");
                name = (String) naverAttributes.get("name");
            } else {
                email = null;
                name = null;
            }
        } else {
            email = null;
            name = null;
        }

        if (email == null) {
            throw new OAuth2AuthenticationException("OAuth2 제공자에서 이메일 정보를 가져올 수 없습니다.");
        }

        User user = userRepository.findByEmail(email)
                .orElseGet(() -> userRepository.save(User.builder()
                        .email(email)
                        .userName(name)
                        .role(UserRole.USER)
                        .build()));

        return new CustomOAuth2User(user, oAuth2User.getAttributes(), registrationId);
    }
}
