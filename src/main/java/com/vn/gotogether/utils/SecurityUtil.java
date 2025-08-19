package com.vn.gotogether.utils;


import com.vn.gotogether.entity.User;
import com.nimbusds.jose.util.Base64;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.*;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class SecurityUtil {

    @Value("${jwt.base64-secret}")
    private String jwtKey;

    @Autowired
    private final JwtEncoder jwtEncoder;

    @Value("${jwt.token-create-validity-in-seconds}")
    private long expire_access;

    @Value("${jwt.token-verify-validity-in-seconds}")
    private long expire_verify;

    public static final MacAlgorithm JWT_ALGORITHM = MacAlgorithm.HS512;

    private SecretKey getSecretKey() {
        byte[] keyBytes = Base64.from(jwtKey).decode();
        return new SecretKeySpec(keyBytes, 0, keyBytes.length, SecurityUtil.JWT_ALGORITHM.getName());
    }

    public Jwt checkRefreshToken(String refreshToken) {
        NimbusJwtDecoder jwtDecoder = NimbusJwtDecoder
                .withSecretKey(getSecretKey())
                .macAlgorithm(SecurityUtil.JWT_ALGORITHM)
                .build();

        try {
            return jwtDecoder.decode(refreshToken);
        } catch (Exception e) {
            System.out.println(">>> JWT error: " + e.getMessage());
            throw e;
        }
    }

    /**
     * Tạo access token từ Authentication (sử dụng khi login thành công).
     */
    public String createToken(Authentication authentication) {
        Instant now = Instant.now();
        Instant validity = now.plus(this.expire_access, ChronoUnit.SECONDS);

        List<String> authorities = authentication.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.toList());

        String username = authentication.getName();
        Object principal = authentication.getPrincipal();
        String userId = null;

        if (principal instanceof UserDetails userDetails && userDetails instanceof UserDetailsImpl userDetailsImpl) {
            userId = userDetailsImpl.getId();
        }

        JwtClaimsSet claims = JwtClaimsSet.builder()
                .subject(username)
                .claim("authorities", authorities)
                .claim("id", userId)
                .issuedAt(now)
                .expiresAt(validity)
                .build();

        JwsHeader jwsHeader = JwsHeader.with(JWT_ALGORITHM).build();
        return this.jwtEncoder.encode(JwtEncoderParameters.from(jwsHeader, claims)).getTokenValue();
    }

    /**
     * Tạo access token trực tiếp từ User.
     */
    public String createTokenFromUser(User user) {
        Instant now = Instant.now();
        Instant validity = now.plus(this.expire_access, ChronoUnit.SECONDS);

        // 👇 Lấy role từ User entity
        String roleName = user.getRole().getName(); // ví dụ: "ROLE_ADMIN"
        List<String> authorities = List.of(roleName);

        JwtClaimsSet claims = JwtClaimsSet.builder()
                .subject(user.getEmail())
                .claim("id", user.getId())
                .claim("authorities", authorities)
                .issuedAt(now)
                .expiresAt(validity)
                .build();

        JwsHeader jwsHeader = JwsHeader.with(JWT_ALGORITHM).build();
        return this.jwtEncoder.encode(JwtEncoderParameters.from(jwsHeader, claims)).getTokenValue();
    }

    /**
     * Lấy username hiện tại từ security context.
     */
    public static Optional<String> getCurrentUserLogin() {
        SecurityContext securityContext = SecurityContextHolder.getContext();
        return Optional.ofNullable(extractPrincipal(securityContext.getAuthentication()));
    }

    private static String extractPrincipal(Authentication authentication) {
        if (authentication == null) return null;

        Object principal = authentication.getPrincipal();
        if (principal instanceof UserDetails userDetails) {
            return userDetails.getUsername();
        } else if (principal instanceof Jwt jwt) {
            return jwt.getSubject();
        } else if (principal instanceof String) {
            return (String) principal;
        }

        return null;
    }

    /**
     * Lấy JWT hiện tại từ SecurityContextHolder.
     */
    public static Optional<String> getCurrentUserJWT() {
        SecurityContext securityContext = SecurityContextHolder.getContext();
        return Optional.ofNullable(securityContext.getAuthentication())
                .filter(authentication -> authentication.getCredentials() instanceof String)
                .map(authentication -> (String) authentication.getCredentials());
    }
}
