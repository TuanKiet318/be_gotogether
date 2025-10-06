package com.vn.gotogether.controller.user;


import com.vn.gotogether.dto.user.*;
import com.vn.gotogether.service.user.UserService;
import com.vn.gotogether.utils.SecurityUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {
    private final UserService userService;

    @GetMapping("/me")
    public ResponseEntity<UserResponse> getCurrentUser() {
        return ResponseEntity.ok(userService.getUserInformation());
    }

    @PostMapping("/register")
    public ResponseEntity<UserResponse> registerUser(@RequestBody UserRegisterRequest request, Authentication authentication) {
        boolean isAdmin = authentication.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
        if(!isAdmin) {return ResponseEntity.status(HttpStatus.FORBIDDEN).build();}
        UserResponse created = userService.registerUser(request);
        return ResponseEntity.ok(created);
    }

    @PostMapping("/register/supplier")
    public ResponseEntity<UserResponse> registerSupplier(@RequestBody UserRegisterRequest request, Authentication authentication) {
        boolean isSupplierManager = authentication.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_SUPPLIER_MANAGER"));
        if(!isSupplierManager) {return ResponseEntity.status(HttpStatus.FORBIDDEN).build();}
        UserResponse created = userService.registerUserSupplier(request);
        return ResponseEntity.ok(created);
    }

    @GetMapping()
    public ResponseEntity<UserResponse> getUserInformation() {
        UserResponse userResponse = userService.getUserInformation();
        return ResponseEntity.ok(userResponse);
    }

    @GetMapping("/by-role/{roleName}")
    public ResponseEntity<List<UserResponse>> getUsersByRole(@PathVariable String roleName) {
        List<UserResponse> users = userService.getUsersByRole(roleName);
        return ResponseEntity.ok(users);
    }

    @PutMapping("/update-info")
    public ResponseEntity<UserResponse> updateUserInfo(@RequestBody UserUpdateRequest request) {
        return ResponseEntity.ok(userService.updateUserInfo(request));
    }

    @PutMapping("/change-password")
    public ResponseEntity<String> changePassword(@RequestBody ChangePasswordRequest request) {
        userService.changePassword(request);
        return ResponseEntity.ok("Thay đổi mật khẩu thành công.");
    }

    @PostMapping("/ping")
    public ResponseEntity<Void> ping() {
        SecurityUtil.getCurrentUserLogin().ifPresent(userService::updateLastActivity);
        return ResponseEntity.ok().build();
    }
    @PostMapping("/forgot-password")
    public ResponseEntity<?> forgotPassword(@RequestBody ForgotPasswordRequest request) {
        userService.forgotPassword(request.getEmail());
        return ResponseEntity.ok("Mật khẩu mới đã được gửi vào email của bạn.");
    }
}
