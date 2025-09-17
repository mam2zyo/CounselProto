package io.notfound.counsel_back.user.service;

import io.notfound.counsel_back.user.dto.UserProfileResponseDto;
import io.notfound.counsel_back.user.entity.User;
import io.notfound.counsel_back.user.entity.UserProfile;
import io.notfound.counsel_back.user.repository.UserProfileRepository;
import io.notfound.counsel_back.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UserProfileService {

    private final UserProfileRepository userProfileRepository;
    private final UserRepository userRepository;

    // 프로필 조회
    @Transactional(readOnly = true)
    public UserProfileResponseDto getProfile(Long userId) {
        UserProfile profile = userProfileRepository.findByUserId(userId)
                .orElseThrow(() -> new RuntimeException("UserProfile not found"));

        return UserProfileResponseDto.builder()
                .gender(profile.getGender())
                .age(profile.getAge())
                .interests(profile.getInterests())
                .concern(profile.getConcern())
                .build();
    }

    // 프로필 수정
    @Transactional
    public void updateProfile(Long userId, String gender, Integer age, String interests, String concern) {
        UserProfile profile = userProfileRepository.findByUserId(userId)
                .orElseThrow(() -> new RuntimeException("UserProfile not found"));

        profile.updateProfile(gender, age, interests, concern);
    }

    // 회원가입 후 최초 프로필 생성
    @Transactional
    public void createProfile(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        UserProfile profile = UserProfile.builder()
                .user(user)
                .gender(null)
                .age(null)
                .interests(null)
                .concern(null)
                .build();

        userProfileRepository.save(profile);
    }
}
