package io.notfound.counsel_back.user.controller;

import io.notfound.counsel_back.user.dto.UserProfileResponseDto;
import io.notfound.counsel_back.user.service.UserProfileService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/users/{userId}/profile")
@RequiredArgsConstructor
public class UserProfileController {

    private final UserProfileService userProfileService;

    // 프로필 조회
    @GetMapping
    public ResponseEntity<UserProfileResponseDto> getProfile(@PathVariable Long userId) {
        return ResponseEntity.ok(userProfileService.getProfile(userId));
    }

    // 프로필 수정
    @PutMapping
    public ResponseEntity<String> updateProfile(
            @PathVariable Long userId,
            @RequestParam(required = false) String gender,
            @RequestParam(required = false) Integer age,
            @RequestParam(required = false) String interests,
            @RequestParam(required = false) String concern
    ) {
        userProfileService.updateProfile(userId, gender, age, interests, concern);
        return ResponseEntity.ok("Profile updated successfully");
    }
}
