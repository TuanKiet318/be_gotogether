package com.vn.gotogether.dto.data;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class InsertDayRequest {
    @NotNull(message = "dayNumber không được null")
    @Min(value = 1, message = "dayNumber phải >= 1")
    private Integer dayNumber;

    @Min(value = 1, message = "count phải >= 1")
    private int count = 1;
}