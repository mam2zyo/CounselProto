package io.notfound.counsel_back.user.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class UserProfileResponseDto {
    private String gender;
    private Integer age;
    private String interests;
    private String concern;
}
