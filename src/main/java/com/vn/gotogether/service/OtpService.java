package com.vn.gotogether.service;

import com.vn.gotogether.entity.User;
import com.vn.gotogether.entity.UserOtp;
import com.vn.gotogether.repository.user.UserOtpRepository;
import com.vn.gotogether.repository.user.UserRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class OtpService {
    private final UserOtpRepository otpRepository;
    private final EmailService emailService;
    private final UserRepository userRepository; // ✅ thêm repository user

    @Transactional
    public void sendOtp(String email) {
        // tìm user theo email
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy user với email: " + email));

        // xóa OTP cũ (nếu có)
        otpRepository.deleteByUserId(user.getId());

        // generate OTP
        String otp = String.valueOf((int) ((Math.random() * 900000) + 100000)); // 6 số

        // save to DB
        UserOtp userOtp = UserOtp.builder()
                .id(UUID.randomUUID().toString())
                .userId(user.getId())
                .otpCode(otp)
                .expiryTime(LocalDateTime.now().plusMinutes(5))
                .build();
        otpRepository.save(userOtp);

        // gửi email
        String subject = "Mã OTP xác nhận";
        String body = "<p>Xin chào " + user.getName() + ",</p>"
                + "<p>Mã OTP của bạn là: <b>" + otp + "</b></p>"
                + "<p>Có hiệu lực trong 5 phút.</p>";
        emailService.sendEmail(email, subject, body);
    }


    public boolean validateOtp(String userId, String otp) {
        Optional<UserOtp> opt = otpRepository.findByUserIdAndOtpCode(userId, otp);
        if (opt.isPresent()) {
            UserOtp userOtp = opt.get();
            if (userOtp.getExpiryTime().isAfter(LocalDateTime.now())) {
                // ✅ kích hoạt user
                User user = userRepository.findById(userId)
                        .orElseThrow(() -> new RuntimeException("User not found"));
                user.setActive(true);
                userRepository.save(user);

                // ✅ xóa OTP sau khi dùng
                otpRepository.delete(userOtp);

                return true;
            }
        }
        return false;
    }
}
