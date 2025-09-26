package io.notfound.counsel_back.auth.dto;

import io.notfound.counsel_back.user.entity.User;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class UserInfoResponse {
    private Long id;
    private String email;

    public static UserInfoResponse from(User user) {
        return UserInfoResponse.builder()
                .id(user.getId())
                .email(user.getEmail())
                .build();
    }
}
