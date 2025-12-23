package com.vn.gotogether.dto.user;

import com.vn.gotogether.enums.Gender;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Builder
public class UserProfileResponse {

    private String id;
    private String name;
    private String email;
    private String avatar;

    private String phone;
    private Gender gender;
    private LocalDate birthday;
    private String bio;
    private String address;

    private LocalDateTime createdAt;
}

