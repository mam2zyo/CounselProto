package io.notfound.counsel_back.user.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "user_profile")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class UserProfile {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String gender;     // 성별
    private Integer age;       // 나이
    private String interests;  // 관심사
    private String concern;    // 고민

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    private User user;

    // 프로필 수정 메서드
    public void updateProfile(String gender, Integer age, String interests, String concern) {
        if (gender != null) this.gender = gender;
        if (age != null) this.age = age;
        if (interests != null) this.interests = interests;
        if (concern != null) this.concern = concern;
    }
}
