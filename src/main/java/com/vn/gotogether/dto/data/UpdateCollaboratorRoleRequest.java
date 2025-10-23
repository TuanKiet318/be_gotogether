package com.vn.gotogether.dto.data;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UpdateCollaboratorRoleRequest {
    private String role; // "VIEWER" hoặc "EDITOR"
}
