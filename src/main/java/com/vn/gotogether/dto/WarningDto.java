package com.vn.gotogether.dto;

import lombok.*;

@Data @NoArgsConstructor @AllArgsConstructor @Builder
public class WarningDto {
    private String itemId;
    private Type type;
    private String message;

    public enum Type { CLOSED, PARTIAL_OPEN, NOT_ENOUGH_TRAVEL, SHORT_VISIT, MISSING_DATA }
}
