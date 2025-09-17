package io.notfound.counsel_back.security.core;

import io.notfound.counsel_back.user.entity.User;
import lombok.Getter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.core.user.OAuth2User;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;

/**
 * OAuth2User 인터페이스를 구현하여, 우리 서비스의 사용자 정보(User)를 담는 DTO 클래스입니다.
 * Spring Security가 인증 완료 후 사용자 정보를 CustomOAuth2User 객체로 관리하게 됩니다.
 */
@Getter
public class CustomOAuth2User implements OAuth2User {

    private final User user;
    private final Map<String, Object> attributes;
    private final String registrationId;

    public CustomOAuth2User(User user, Map<String, Object> attributes, String registrationId) {
        this.user = user;
        this.attributes = attributes;
        this.registrationId = registrationId;
    }

    @Override
    public Map<String, Object> getAttributes() {
        return attributes;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return Collections.singletonList(new SimpleGrantedAuthority(user.getRole().name()));
    }

    @Override
    public String getName() {
        // OAuth2User의 nameAttributeKey와 매칭될 이름을 반환합니다.
        // Google의 경우 'sub', Naver의 경우 'id'를 고유 식별자로 사용합니다.
        if ("google".equals(registrationId)) {
            return (String) attributes.get("sub");
        } else if ("naver".equals(registrationId)) {
            // 네이버는 'response'라는 Map 안에 'id'가 있습니다.
            Map<String, Object> naverAttributes = (Map<String, Object>) attributes.get("response");
            if (naverAttributes != null) {
                return (String) naverAttributes.get("id");
            }
        }
        return null;
    }
}