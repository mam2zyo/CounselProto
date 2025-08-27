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

    /**
     * OAuth2UserRequest를 받아 사용자 정보를 로드하고,
     * 해당 사용자가 DB에 없으면 저장 후 인증 객체를 반환합니다.
     *
     * @param userRequest 사용자 정보 로드 요청 객체
     * @return CustomOAuth2User 객체 (사용자 정보와 권한을 포함)
     * @throws OAuth2AuthenticationException 인증 과정에서 예외 발생 시
     */
    @Override
    @Transactional
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
        // 1. 부모 클래스의 loadUser 메서드를 호출하여 기본 OAuth2User 정보를 가져옵니다.
        OAuth2User oAuth2User = super.loadUser(userRequest);

        // 2. 현재 로그인에 사용된 Provider(google, naver 등)를 식별합니다.
        String registrationId = userRequest.getClientRegistration().getRegistrationId();

        // 3. Provider별로 사용자 정보를 파싱하여 추출합니다.
        // final 변수로 선언하여 람다에서 참조할 수 있도록 합니다.
        final String email;
        final String name;

        if ("google".equals(registrationId)) {
            email = oAuth2User.getAttribute("email");
            // Google의 경우 "profile" 스코프가 없어 name을 가져올 수 없으므로, email을 name으로 사용하거나 다른 로직을 추가해야 합니다.
            // 여기서는 일단 이메일을 이름으로 사용합니다.
            String tempName = oAuth2User.getAttribute("name");
            name = (tempName != null) ? tempName : email;
        } else if ("naver".equals(registrationId)) {
            // Naver는 사용자 정보가 'response'라는 Map 안에 담겨 있습니다.
            Map<String, Object> naverAttributes = oAuth2User.getAttribute("response");
            if (naverAttributes != null) {
                email = (String) naverAttributes.get("email");
                name = (String) naverAttributes.get("name");
            } else {
                email = null;
                name = null;
            }
        } else {
            // 지원하지 않는 Provider의 경우 null로 초기화
            email = null;
            name = null;
        }

        // 4. DB에서 해당 이메일을 가진 사용자를 찾습니다.
        User user = userRepository.findByEmail(email)
                .orElseGet(() -> {
                    // 5. 사용자가 DB에 없으면 새로 생성하여 저장합니다.
                    User newUser = User.builder()
                            .email(email)
                            .name(name)
                            .role(UserRole.USER) // 기본 역할은 USER
                            .build();
                    return userRepository.save(newUser);
                });

        // 6. 우리 서비스의 CustomOAuth2User 객체를 생성하여 반환합니다.
        return new CustomOAuth2User(user, oAuth2User.getAttributes(), registrationId);
    }
}