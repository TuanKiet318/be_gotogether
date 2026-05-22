package com.vn.gotogether.dto.auth;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class GoogleTokenInfoResponse {
    private String aud;
    private String email;

    @JsonProperty("email_verified")
    private String emailVerified;

    private String name;
    private String picture;
    private String sub;
}