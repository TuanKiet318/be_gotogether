package com.vn.gotogether.dto.data;

import lombok.*;

import java.time.Instant;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class TagResponse {
    private String id;
    private String code;
    private String name;
    private String description;
    private Instant createdAt;
}
