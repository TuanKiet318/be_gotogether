package com.vn.gotogether.repository.auth;



import com.vn.gotogether.entity.RefreshToken;
import com.vn.gotogether.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface RefreshTokenRepository extends JpaRepository<RefreshToken, String> {

    Optional<RefreshToken> findByTokenAndDeviceId(String token, String deviceId);

    void deleteByUserAndDeviceId(User user, String deviceId);

    void deleteAllByUser(User user);

    List<RefreshToken> findByUser(User user);
}
