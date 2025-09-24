package com.vn.gotogether.repository.user;

import com.vn.gotogether.entity.UserOtp;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserOtpRepository extends JpaRepository<UserOtp, String> {
    Optional<UserOtp> findByUserIdAndOtpCode(String userId, String otpCode);

    // ✅ lấy OTP mới nhất theo userId
    Optional<UserOtp> findTopByUserIdOrderByExpiryTimeDesc(String userId);

    // ✅ xóa OTP cũ khi gửi lại
    void deleteByUserId(String userId);
}
