package com.vn.gotogether.dto.user;

import lombok.*;
import lombok.experimental.FieldDefaults;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class     UserUpdateRequest {
    String id;
    String name;
    String email;
    String password;
    String contactLink;
}
