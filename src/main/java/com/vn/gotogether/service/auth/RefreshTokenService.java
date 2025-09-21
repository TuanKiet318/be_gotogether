package com.vn.gotogether.service.auth;



import com.vn.gotogether.entity.RefreshToken;
import com.vn.gotogether.entity.User;
import com.vn.gotogether.exception.UnauthorizedException;
import com.vn.gotogether.repository.auth.RefreshTokenRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class RefreshTokenService {

    private final RefreshTokenRepository refreshTokenRepository;

    @Value("${jwt.token-verify-validity-in-seconds}")
    private Long refreshTokenExpiration;

    /**
     * Tạo refresh token cho user trên 1 thiết bị cụ thể
     */
    public RefreshToken create(User user, String deviceId, String userAgent, String ipAddress) {
        RefreshToken refreshToken = RefreshToken.builder()
                .token(UUID.randomUUID().toString())
                .deviceId(deviceId)
                .user(user)
                .userAgent(userAgent)
                .ipAddress(ipAddress)
                .expiryDate(Instant.now().plusSeconds(refreshTokenExpiration))
                .build();
        return refreshTokenRepository.save(refreshToken);
    }

    /**
     * Kiểm tra token có tồn tại và còn hạn hay không
     */
    public RefreshToken verify(String token, String deviceId) {
        return refreshTokenRepository.findByTokenAndDeviceId(token, deviceId)
                .map(rt -> {
                    if (rt.getExpiryDate().isBefore(Instant.now())) {
                        refreshTokenRepository.delete(rt);
                        throw new RuntimeException("Refresh token đã hết hạn.");
                    }
                    return rt;
                }).orElseThrow(() -> new RuntimeException("Refresh token không hợp lệ."));
    }

    /**
     * Xoá token trên thiết bị cụ thể
     */
    @Transactional
    public void revoke(String refreshToken, String deviceId) {
        RefreshToken token = refreshTokenRepository.findByTokenAndDeviceId(refreshToken, deviceId)
                .orElseThrow(() -> new UnauthorizedException("Token không tồn tại hoặc đã bị thu hồi"));

        refreshTokenRepository.delete(token);
    }


    /**
     * Xoá tất cả refresh token của user (logout toàn bộ)
     */
    public void revokeAll(User user) {
        refreshTokenRepository.deleteAllByUser(user);
    }

    /**
     * Lấy token theo giá trị và thiết bị
     */
    public Optional<RefreshToken> getByTokenAndDevice(String token, String deviceId) {
        return refreshTokenRepository.findByTokenAndDeviceId(token, deviceId);
    }
}
