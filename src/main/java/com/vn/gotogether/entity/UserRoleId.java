package com.vn.gotogether.entity;

import jakarta.persistence.*;
import lombok.*;

import java.io.Serializable;

@Embeddable
@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserRoleId implements Serializable {
    @Column(name = "user_id")
    private String userId;

    @Column(name = "role_id")
    private String roleId;
}
