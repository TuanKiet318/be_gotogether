package com.vn.gotogether.dto.user;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UserRegisterRequest {
    private String name;
    private String email;
    private String roleName;
    private String password;
}
