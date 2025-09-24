package com.vn.gotogether.controller.auth;



import com.vn.gotogether.dto.ApiResponse;
import com.vn.gotogether.dto.auth.LoginDto;
import com.vn.gotogether.dto.user.UserRegisterRequest;
import com.vn.gotogether.dto.user.UserResponse;
import com.vn.gotogether.entity.RefreshToken;
import com.vn.gotogether.entity.User;
import com.vn.gotogether.exception.UnauthorizedException;
import com.vn.gotogether.service.OtpService;
import com.vn.gotogether.service.auth.RefreshTokenService;
import com.vn.gotogether.service.user.UserService;
import com.vn.gotogether.utils.SecurityUtil;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.LockedException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {
    private final OtpService otpService;
    private final SecurityUtil securityUtil;
    private final AuthenticationManagerBuilder authenticationManagerBuilder;
    private final UserService userService;
    private final RefreshTokenService refreshTokenService;
    @Value("${jwt.token-verify-validity-in-seconds}")
    private long refreshTokenExpiration;


    @PostMapping("/login")
    public ResponseEntity<?> login(
            @RequestBody LoginDto loginDto,
            @RequestHeader(value = "X-Device-Id", required = false) String deviceId,
            HttpServletRequest request
    ) {
        UsernamePasswordAuthenticationToken authenticationToken =
                new UsernamePasswordAuthenticationToken(loginDto.getEmail(), loginDto.getPassword());

        try {
            // Xác thực đăng nhập
            Authentication authentication = authenticationManagerBuilder.getObject().authenticate(authenticationToken);

            // Lấy thông tin người dùng từ DB
            User user = userService.findByEmail(loginDto.getEmail())
                    .orElseThrow(() -> new UnauthorizedException("Tài khoản không tồn tại"));

            // ✅ Cập nhật lastLogin & previousLogin
            user.setPreviousLogin(user.getLastLogin());
            user.setLastLogin(LocalDateTime.now());
            userService.save(user); // Đảm bảo có phương thức save()

            // Tạo access token
            String accessToken = securityUtil.createToken(authentication);

            if (deviceId == null || deviceId.isBlank()) {
                deviceId = UUID.randomUUID().toString();
            }

            RefreshToken refreshToken = refreshTokenService.create(
                    user,
                    deviceId,
                    request.getHeader("User-Agent"),
                    request.getRemoteAddr()
            );

            ResponseCookie cookie = ResponseCookie.from("refreshToken", refreshToken.getToken())
                    .httpOnly(true)
                    .secure(true)
                    .path("/")
                    .maxAge(refreshTokenExpiration)
                    .build();

            return ResponseEntity.ok()
                    .header(HttpHeaders.SET_COOKIE, cookie.toString())
                    .header("X-Device-Id", deviceId)
                    .body(ApiResponse.success(Map.of("accessToken", accessToken)));

        } catch (LockedException e) {
            throw new UnauthorizedException("Tài khoản bị khóa");
        } catch (DisabledException e) {
            throw new UnauthorizedException("Tài khoản chưa được kích hoạt");
        } catch (BadCredentialsException e) {
            throw new UnauthorizedException("Email hoặc mật khẩu không đúng");
        }
    }


    @GetMapping("/refresh")
    public ResponseEntity<?> refreshAccessToken(
            @CookieValue(value = "refreshToken", defaultValue = "") String refreshToken,
            @RequestHeader("X-Device-Id") String deviceId
    ) {
        if (refreshToken.isBlank()) {
            throw new UnauthorizedException("Bạn chưa đăng nhập hoặc thiếu refresh token");
        }

        RefreshToken token = refreshTokenService.verify(refreshToken, deviceId);
        User user = token.getUser();

        String newAccessToken = securityUtil.createTokenFromUser(user);
        RefreshToken newRefresh = refreshTokenService.create(
                user,
                deviceId,
                token.getUserAgent(),
                token.getIpAddress()
        );

        ResponseCookie cookie = ResponseCookie.from("refreshToken", newRefresh.getToken())
                .httpOnly(true)
                .secure(true)
                .path("/")
                .maxAge(refreshTokenExpiration)
                .build();

        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, cookie.toString())
                .header("X-Device-Id", deviceId)
                .body(ApiResponse.success(Map.of("accessToken", newAccessToken)));
    }


    @PostMapping("/logout")
    public ResponseEntity<?> logout(
            @CookieValue("refreshToken") String refreshToken,
            @RequestHeader("X-Device-Id") String deviceId
    ) {
        refreshTokenService.revoke(refreshToken, deviceId); // tìm theo token + deviceId và xóa

        ResponseCookie clearCookie = ResponseCookie.from("refreshToken", "")
                .httpOnly(true)
                .secure(true)
                .path("/")
                .maxAge(0)
                .build();

        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, clearCookie.toString())
                .body(ApiResponse.success("Đăng xuất thành công", null));
    }

    @GetMapping("/verify")
    public ResponseEntity<?> verifyToken(Authentication authentication) {
        User user = userService.findByEmail(authentication.getName())
                .orElseThrow(() -> new UnauthorizedException("Không tìm thấy tài khoản"));
        return ResponseEntity.ok(ApiResponse.success(Map.of(
                "email", authentication.getName(),
                "roles", authentication.getAuthorities(),
                "active", user.isActive()
        )));
    }

    @PostMapping("/register")
    public ResponseEntity<UserResponse> register(@RequestBody UserRegisterRequest request) {
        request.setRoleName("ROLE_USER");
        UserResponse response = userService.registerUser(request);
        return ResponseEntity.ok(response);
    }


    @PostMapping("/send-otp")
    public ResponseEntity<String> sendOtp(@RequestParam String userId, @RequestParam String email) {
        otpService.sendOtp(userId, email);
        return ResponseEntity.ok("OTP đã gửi tới email");
    }

    @PostMapping("/verify-otp")
    public ResponseEntity<Boolean> verifyOtp(@RequestParam String userId, @RequestParam String otp) {
        boolean valid = otpService.validateOtp(userId, otp);
        return ResponseEntity.ok(valid);
    }
}
