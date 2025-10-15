package com.vn.gotogether.dto.data;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class RenameItineraryRequest {
    @NotBlank(message = "Tên lịch trình không được để trống")
    private String newTitle;
}
