package com.vn.gotogether.dto.user;

import com.vn.gotogether.enums.Gender;
import lombok.Data;

import java.time.LocalDate;

@Data
public class UpdateProfileRequest {

    private String name;
    private String phone;
    private Gender gender;
    private LocalDate birthday;
    private String bio;
    private String address;
}

