package com.vn.gotogether.service.auth;

import com.vn.gotogether.dto.auth.GoogleTokenInfoResponse;
import com.vn.gotogether.exception.UnauthorizedException;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestClient;
import org.springframework.web.util.UriComponentsBuilder;

@Service
@RequiredArgsConstructor
public class GoogleAuthService {

    @Value("${google.oauth.client-id:}")
    private String googleClientId;

    public GoogleTokenInfoResponse verifyIdToken(String idToken) {
        if (idToken == null || idToken.isBlank()) {
            throw new UnauthorizedException("Thiếu Google idToken");
        }

        if (googleClientId == null || googleClientId.isBlank()) {
            throw new UnauthorizedException("Google client id chưa được cấu hình");
        }

        String url = UriComponentsBuilder
                .fromHttpUrl("https://oauth2.googleapis.com/tokeninfo")
                .queryParam("id_token", idToken)
                .toUriString();

        GoogleTokenInfoResponse tokenInfo;
        try {
            tokenInfo = RestClient.create()
                    .get()
                    .uri(url)
                    .retrieve()
                    .body(GoogleTokenInfoResponse.class);
        } catch (RestClientException e) {
            throw new UnauthorizedException("Không thể xác thực Google token");
        }

        if (tokenInfo == null) {
            throw new UnauthorizedException("Không thể xác thực Google token");
        }

        if (!googleClientId.equals(tokenInfo.getAud())) {
            throw new UnauthorizedException("Google token không hợp lệ cho ứng dụng này");
        }

        if (tokenInfo.getEmail() == null || tokenInfo.getEmail().isBlank()) {
            throw new UnauthorizedException("Google account không có email hợp lệ");
        }

        if (tokenInfo.getEmailVerified() == null || !Boolean.parseBoolean(tokenInfo.getEmailVerified())) {
            throw new UnauthorizedException("Email Google chưa được xác minh");
        }

        return tokenInfo;
    }
}