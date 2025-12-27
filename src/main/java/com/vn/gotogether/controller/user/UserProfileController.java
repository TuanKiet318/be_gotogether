package com.vn.gotogether.controller.user;

import com.vn.gotogether.dto.user.ChangePasswordRequest;
import com.vn.gotogether.dto.user.UpdateProfileRequest;
import com.vn.gotogether.dto.user.UserProfileResponse;
import com.vn.gotogether.dto.user.UserResponse;
import com.vn.gotogether.service.user.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;


@RestController
@RequestMapping("/api/users/profile")
@RequiredArgsConstructor
public class UserProfileController {

    private final UserService userService;

    @GetMapping
    public ResponseEntity<UserProfileResponse> getProfile() {
        return ResponseEntity.ok(userService.getUserProfile());
    }

    @PutMapping
    public ResponseEntity<UserProfileResponse> updateProfile(
            @RequestBody UpdateProfileRequest request) {
        return ResponseEntity.ok(userService.updateProfile(request));
    }
}


