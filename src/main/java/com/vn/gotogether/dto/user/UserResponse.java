package com.vn.gotogether.dto.user;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserResponse {
    String id;
    String name;
    String email;
    String role;
    String avatar;
    LocalDateTime lastLogin;
    LocalDateTime previousLogin;
    LocalDateTime createdAt;
    LocalDateTime passwordUpdatedAt;
    boolean online;
    boolean active;

}