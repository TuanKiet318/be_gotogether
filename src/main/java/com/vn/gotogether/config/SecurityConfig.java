package com.vn.gotogether.config;


import com.vn.gotogether.utils.SecurityUtil;
import com.nimbusds.jose.jwk.source.ImmutableSecret;
import com.nimbusds.jose.util.Base64;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtEncoder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.security.web.SecurityFilterChain;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.util.Collections;
import java.util.List;


@Configuration
public class SecurityConfig {

    @Value("${jwt.base64-secret}")
    private String jwtKey;

    @Autowired
    private JwtAuthEntryPoint jwtAuthEntryPoint;

    private final String[] API_ALLOWED = {
            "/", "/api/auth/login", "/api/auth/refresh",  "/api/account/reset/**","/api/","/uploads/**",
//            "/api/posts/**", "/api/promotions/**", "/api/v1/skills/**", "/api/v1/email","/api/hashtags/**",
            "/v3/api-docs/**", "/swagger-ui/**", "/swagger-ui.html", "/api/prices/**"
    };

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .authorizeHttpRequests(auth -> auth
                        // ✅ Cho phép các endpoint public mà không cần token
                        .requestMatchers(
                                "/", "/api/auth/login", "/api/auth/refresh",
                                "/api/account/reset/**", "/uploads/**", "/api/prices/**",
                                "/v3/api-docs/**", "/swagger-ui/**", "/swagger-ui.html"
                        ).permitAll()

                        // ✅ Chỉ các API admin mới yêu cầu xác thực
                        .requestMatchers("/api/admin/**").authenticated()

                        // ✅ Các endpoint khác cũng không cần auth (nếu muốn chặn, có thể thay = .authenticated())
                        .anyRequest().permitAll()
                )
                .oauth2ResourceServer(oauth2 -> oauth2
                        .authenticationEntryPoint(jwtAuthEntryPoint)
                        .jwt(jwt -> jwt.jwtAuthenticationConverter(token -> {
                            List<String> permission = token.getClaimAsStringList("authorities");
                            if (permission == null) permission = Collections.emptyList();
                            return new JwtAuthenticationToken(
                                    token,
                                    permission.stream().map(SimpleGrantedAuthority::new).toList()
                            );
                        }))
                )
                .cors(Customizer.withDefaults())
                .csrf(AbstractHttpConfigurer::disable)
                .formLogin(AbstractHttpConfigurer::disable)
                .sessionManagement(ss -> ss.sessionCreationPolicy(SessionCreationPolicy.STATELESS));

        return http.build();
    }

    private SecretKey getSecretKey() {
        byte[] keyBytes = Base64.from(jwtKey).decode();
        return new SecretKeySpec(keyBytes, 0, keyBytes.length, SecurityUtil.JWT_ALGORITHM.getName());
    }

    @Bean
    public JwtEncoder jwtEncoder() {
        return new NimbusJwtEncoder(new ImmutableSecret<>(getSecretKey()));
    }

    @Bean
    public JwtDecoder jwtDecoder() {
        NimbusJwtDecoder jwtDecoder = NimbusJwtDecoder
                .withSecretKey(getSecretKey())
                .macAlgorithm(SecurityUtil.JWT_ALGORITHM)
                .build();
        return token -> {
            try {
                return jwtDecoder.decode(token);
            } catch (Exception e) {
                System.out.println(">>> JWT error: " + e.getMessage());
                throw e;
            }
        };
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}


