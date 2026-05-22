package com.vn.gotogether.dto.auth;

import lombok.Data;

@Data
public class GoogleLoginRequest {
    private String idToken;
}